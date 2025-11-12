package com.lostark.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.dto.CharacterStatDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final LostArkApiService lostArkApiService;
    private final ObjectMapper objectMapper;
    private final SearchHistoryService searchHistoryService;

    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    
    @Transactional
    public CharacterProfileDto getCharacterProfile(String characterName, String userId) {
        log.info("캐릭터 검색 시작: {}", characterName);
        
        // 검색 히스토리 저장
        if (userId != null) {
            searchHistoryService.addSearchHistory(userId, characterName);
        }
        
        // 1. DB에서 캐시 확인
        Optional<Character> cachedCharacter = characterRepository.findByCharacterName(characterName);
        
        if (cachedCharacter.isPresent()) {
            Character character = cachedCharacter.get();
            boolean cacheFresh = Duration.between(character.getUpdatedAt(), LocalDateTime.now())
                    .compareTo(CACHE_DURATION) < 0;
            boolean needsUpgrade = character.getTitle() == null || character.getStatsJson() == null || character.getCombatPower() == null;
            
            if (cacheFresh && !needsUpgrade) {
                log.info("캐시된 데이터 반환: {}", characterName);
                return convertToDto(character);
            }
            
            if (needsUpgrade) {
                log.info("캐시 데이터에 누락된 필드가 있어 재요청: {}", characterName);
            }
        }
        
        // 2. API 호출
        log.info("로스트아크 API 호출: {}", characterName);
        try {
            CharacterProfileDto profile = lostArkApiService.getCharacterProfile(characterName).block();

            if (profile != null && profile.getCombatPower() == null) {
                try {
                    ArmoryDto armoryDto = lostArkApiService.getCharacterArmory(characterName).block();
                    if (armoryDto != null && armoryDto.getProfile() != null && armoryDto.getProfile().getCombatPower() != null) {
                        profile.setCombatPower(armoryDto.getProfile().getCombatPower());
                    }
                } catch (Exception ex) {
                    log.warn("전투력 정보를 가져오지 못했습니다: {}", ex.getMessage());
                }
            }

            if (profile == null || profile.getCharacterName() == null) {
                log.error("캐릭터를 찾을 수 없음: {}", characterName);
                throw new CharacterNotFoundException("캐릭터를 찾을 수 없습니다: " + characterName);
            }

            log.info("API 호출 성공: {}", profile.getCharacterName());

            // 3. DB에 저장 또는 업데이트
            Character character = cachedCharacter.orElse(new Character());
            updateCharacterFromDto(character, profile);
            characterRepository.save(character);

            return profile;
        } catch (CharacterNotFoundException e) {
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
    
    private void updateCharacterFromDto(Character character, CharacterProfileDto dto) {
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
}
