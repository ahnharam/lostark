package com.lostark.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.dto.CharacterStatDto;
import com.lostark.backend.dto.CollectibleDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.CharacterSnapshot;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import com.lostark.backend.repository.CharacterRepository;
import com.lostark.backend.repository.CharacterSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterSnapshotRepository characterSnapshotRepository;
    private final LostArkProfileDomainService lostArkProfileDomainService;
    private final ObjectMapper objectMapper;
    private final SearchHistoryService searchHistoryService;

    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalTime RESET_CUTOFF_TIME = LocalTime.of(6, 0);
    private static final ExecutorService asyncExecutor = Executors.newFixedThreadPool(10);
    
    @Transactional
    public CharacterProfileDto getCharacterProfile(String characterName, String userId, boolean forceRefresh) {
        log.info("캐릭터 검색 시작: {}", characterName);
        
        // 검색 히스토리 저장
        if (userId != null) {
            searchHistoryService.addSearchHistory(userId, characterName);
        }
        
        Optional<Character> cachedCharacter = characterRepository.findByCharacterName(characterName);
        LocalDate currentResetDate = resolveResetDate(ZonedDateTime.now(ZONE_SEOUL));
        
        if (cachedCharacter.isPresent() && !forceRefresh) {
            Character character = cachedCharacter.get();
            boolean hasTimestamp = character.getUpdatedAt() != null;
            boolean cacheFresh = hasTimestamp &&
                    Duration.between(character.getUpdatedAt(), LocalDateTime.now()).compareTo(CACHE_DURATION) < 0;
            boolean needsUpgrade =
                    character.getTitle() == null ||
                    character.getStatsJson() == null ||
                    character.getCombatPower() == null ||
                    character.getHonorPoint() == null;
            boolean sameResetWindow = hasTimestamp &&
                    resolveResetDate(character.getUpdatedAt()).equals(currentResetDate);
            
            if (cacheFresh && !needsUpgrade && sameResetWindow) {
                log.info("캐시된 데이터 반환: {}", characterName);
                return convertToDto(character);
            }
            
            if (needsUpgrade) {
                log.info("캐시 데이터에 누락된 필드가 있어 재요청: {}", characterName);
            } else if (!sameResetWindow) {
                log.info("리셋 시각이 지나 캐시를 무효화: {}", characterName);
            }
        }
        
        // 2. API 호출 (병렬 처리)
        log.info("로스트아크 API 호출 (병렬): {}", characterName);
        try {
            // 병렬로 API 호출
            CompletableFuture<CharacterProfileDto> profileFuture = CompletableFuture.supplyAsync(
                () -> lostArkProfileDomainService.fetchCharacterProfile(characterName),
                asyncExecutor
            );
            CompletableFuture<List<CollectibleDto>> collectiblesFuture = CompletableFuture.supplyAsync(
                () -> lostArkProfileDomainService.fetchCollectibles(characterName),
                asyncExecutor
            );

            // 두 API 호출이 모두 완료될 때까지 대기
            CharacterProfileDto profile = profileFuture.join();
            List<CollectibleDto> collectibles = collectiblesFuture.join();
            Double collectionScore = calculateCollectionScore(collectibles);

            log.info("API 호출 성공 (병렬): {}", profile.getCharacterName());

            // 3. DB에 저장 또는 업데이트 (비동기)
            Character character = cachedCharacter.orElse(new Character());
            updateCharacterFromDto(character, profile, collectionScore);

            // DB 저장을 비동기로 처리
            LocalDate finalResetDate = currentResetDate;
            CompletableFuture.runAsync(() -> {
                try {
                    Character savedCharacter = characterRepository.save(character);
                    captureSnapshot(savedCharacter, profile, collectionScore, finalResetDate);
                    log.debug("DB 저장 완료 (비동기): {}", characterName);
                } catch (Exception e) {
                    log.error("DB 저장 실패 (비동기): {} - {}", characterName, e.getMessage(), e);
                }
            }, asyncExecutor);

            return profile;
        } catch (CharacterNotFoundException e) {
            if (cachedCharacter.isPresent()) {
                log.warn("API에서 캐릭터를 찾지 못해 캐시 데이터로 대체: {}", characterName);
                return convertToDto(cachedCharacter.get());
            }
            throw e;
        } catch (Exception e) {
            log.error("캐릭터 검색 실패: {} - {}", characterName, e.getMessage(), e);
            throw new ApiException("로스트아크 API 호출 중 오류가 발생했습니다.", e);
        }
    }
    
    private CharacterProfileDto convertToDto(Character character) {
        CharacterProfileDto dto = new CharacterProfileDto();
        dto.setCharacterName(character.getCharacterName());
        dto.setServerName(character.getServerName());
        dto.setCharacterClassName(character.getCharacterClassName());
        dto.setCharacterLevel(character.getCharacterLevel());
        dto.setItemAvgLevel(character.getItemAvgLevel());
        dto.setItemMaxLevel(character.getItemMaxLevel());
        dto.setCharacterImage(character.getCharacterImage());
        dto.setTitle(character.getTitle());
        dto.setExpeditionLevel(character.getExpeditionLevel() != null ? 
                Integer.parseInt(character.getExpeditionLevel()) : null);
        dto.setPvpGradeName(character.getPvpGradeName());
        dto.setGuildName(character.getGuildName());
        dto.setCombatPower(character.getCombatPower());
        dto.setHonorPoint(character.getHonorPoint());
        List<CharacterStatDto> stats = parseStats(character.getStatsJson());
        if (stats != null) {
            dto.setStats(stats);
        } else if (character.getApiResponse() != null) {
            try {
                CharacterProfileDto cachedDto = objectMapper.readValue(character.getApiResponse(), CharacterProfileDto.class);
                dto.setStats(cachedDto.getStats());
                if (dto.getTitle() == null) {
                    dto.setTitle(cachedDto.getTitle());
                }
                if (dto.getCombatPower() == null) {
                    dto.setCombatPower(cachedDto.getCombatPower());
                }
                if (dto.getHonorPoint() == null) {
                    dto.setHonorPoint(cachedDto.getHonorPoint());
                }
            } catch (JsonProcessingException e) {
                dto.setStats(null);
            }
        }
        return dto;
    }
    
    private List<CharacterStatDto> parseStats(String statsJson) {
        if (statsJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(statsJson, new TypeReference<List<CharacterStatDto>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    private Double calculateCollectionScore(List<CollectibleDto> collectibles) {
        if (collectibles == null || collectibles.isEmpty()) {
            return null;
        }
        return collectibles.stream()
                .map(CollectibleDto::getPoint)
                .filter(point -> point != null && point > 0)
                .mapToDouble(Integer::doubleValue)
                .sum();
    }

    private void updateCharacterFromDto(Character character, CharacterProfileDto dto, Double collectionScore) {
        character.setCharacterName(dto.getCharacterName());
        character.setServerName(dto.getServerName());
        character.setCharacterClassName(dto.getCharacterClassName());
        character.setCharacterLevel(dto.getCharacterLevel());
        character.setItemAvgLevel(dto.getItemAvgLevel());
        character.setItemMaxLevel(dto.getItemMaxLevel());
        character.setCharacterImage(dto.getCharacterImage());
        character.setTitle(dto.getTitle());
        character.setExpeditionLevel(dto.getExpeditionLevel() != null ? 
                dto.getExpeditionLevel().toString() : null);
        character.setPvpGradeName(dto.getPvpGradeName());
        character.setGuildName(dto.getGuildName());
        character.setCombatPower(dto.getCombatPower());
        character.setHonorPoint(dto.getHonorPoint());
        character.setCollectionScore(collectionScore);
        try {
            character.setStatsJson(dto.getStats() != null ?
                    objectMapper.writeValueAsString(dto.getStats()) : null);
        } catch (JsonProcessingException e) {
            character.setStatsJson(null);
        }
        
        try {
            character.setApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            character.setApiResponse(null);
        }
    }

    private void captureSnapshot(Character character, CharacterProfileDto dto, Double collectionScore, LocalDate resetDate) {
        if (character == null || dto == null || resetDate == null) {
            return;
        }

        try {
            saveOrUpdateSnapshot(character, dto, collectionScore, resetDate);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 중복 키 예외 발생 시 기존 스냅샷 조회 후 업데이트
            log.debug("스냅샷 중복 감지, 기존 스냅샷 업데이트 시도: {} - {}", character.getCharacterName(), resetDate);
            try {
                saveOrUpdateSnapshot(character, dto, collectionScore, resetDate);
            } catch (Exception retryException) {
                log.warn("스냅샷 업데이트 재시도 실패: {} - {}", character.getCharacterName(), retryException.getMessage());
            }
        }
    }

    private void saveOrUpdateSnapshot(Character character, CharacterProfileDto dto, Double collectionScore, LocalDate resetDate) {
        CharacterSnapshot snapshot = characterSnapshotRepository
                .findByCharacterAndResetDate(character, resetDate)
                .orElseGet(CharacterSnapshot::new);

        snapshot.setCharacter(character);
        snapshot.setResetDate(resetDate);
        snapshot.setCapturedAt(LocalDateTime.now(ZONE_SEOUL));
        snapshot.setCharacterName(dto.getCharacterName());
        snapshot.setServerName(dto.getServerName());
        snapshot.setCharacterClassName(dto.getCharacterClassName());
        snapshot.setCharacterLevel(dto.getCharacterLevel());
        snapshot.setItemAvgLevel(dto.getItemAvgLevel());
        snapshot.setItemMaxLevel(dto.getItemMaxLevel());
        snapshot.setCharacterImage(dto.getCharacterImage());
        snapshot.setTitle(dto.getTitle());
        snapshot.setExpeditionLevel(dto.getExpeditionLevel() != null ?
                dto.getExpeditionLevel().toString() : null);
        snapshot.setPvpGradeName(dto.getPvpGradeName());
        snapshot.setGuildName(dto.getGuildName());
        snapshot.setHonorPoint(dto.getHonorPoint());
        snapshot.setCombatPower(dto.getCombatPower());
        snapshot.setCollectionScore(collectionScore);

        try {
            snapshot.setStatsJson(dto.getStats() != null ?
                    objectMapper.writeValueAsString(dto.getStats()) : null);
        } catch (JsonProcessingException e) {
            snapshot.setStatsJson(null);
        }

        try {
            snapshot.setApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            snapshot.setApiResponse(null);
        }

        characterSnapshotRepository.save(snapshot);
    }

    private LocalDate resolveResetDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return resolveResetDate(dateTime.atZone(ZONE_SEOUL));
    }

    /**
     * 06:00 KST 이전은 전날을 반환.
     */
    private LocalDate resolveResetDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        LocalTime time = zonedDateTime.toLocalTime();
        LocalDate date = zonedDateTime.toLocalDate();
        if (time.isBefore(RESET_CUTOFF_TIME)) {
            return date.minusDays(1);
        }
        return date;
    }
}
