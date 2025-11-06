package com.lostark.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
            // 캐시가 1시간 이내면 DB 데이터 반환
            if (Duration.between(character.getUpdatedAt(), LocalDateTime.now()).compareTo(CACHE_DURATION) < 0) {
                log.info("캐시된 데이터 반환: {}", characterName);
                return convertToDto(character);
            }
        }
        
        // 2. API 호출
        log.info("로스트아크 API 호출: {}", characterName);
        try {
            CharacterProfileDto profile = lostArkApiService.getCharacterProfile(characterName).block();
            
            log.info("API 응답 받음: profile={}", profile);
            
            if (profile == null) {
                log.error("API 응답이 null입니다: {}", characterName);
                throw new RuntimeException("캐릭터를 찾을 수 없습니다.");
            }
            
            log.info("profile.getCharacterName()={}", profile.getCharacterName());
            log.info("profile.getServerName()={}", profile.getServerName());
            log.info("profile.getItemMaxLevel()={}", profile.getItemMaxLevel());
            
            if (profile.getCharacterName() == null) {
                log.error("캐릭터를 찾을 수 없음: {}", characterName);
                throw new RuntimeException("캐릭터를 찾을 수 없습니다.");
            }
            
            log.info("API 호출 성공: {}", profile.getCharacterName());
            
            // 3. DB에 저장 또는 업데이트
            Character character = cachedCharacter.orElse(new Character());
            updateCharacterFromDto(character, profile);
            characterRepository.save(character);
            
            return profile;
        } catch (Exception e) {
            log.error("캐릭터 검색 실패: {} - {}", characterName, e.getMessage(), e);
            throw new RuntimeException("캐릭터를 찾을 수 없습니다.");
        }
    }
    
    private CharacterProfileDto convertToDto(Character character) {
        CharacterProfileDto dto = new CharacterProfileDto();
        dto.setCharacterName(character.getCharacterName());
        dto.setServerName(character.getServerName());
        dto.setCharacterClassName(character.getCharacterClassName());
        dto.setItemAvgLevel(character.getItemAvgLevel());
        dto.setItemMaxLevel(character.getItemMaxLevel());
        dto.setCharacterImage(character.getCharacterImage());
        dto.setExpeditionLevel(character.getExpeditionLevel() != null ? 
                Integer.parseInt(character.getExpeditionLevel()) : null);
        dto.setPvpGradeName(character.getPvpGradeName());
        dto.setGuildName(character.getGuildName());
        return dto;
    }
    
    private void updateCharacterFromDto(Character character, CharacterProfileDto dto) {
        character.setCharacterName(dto.getCharacterName());
        character.setServerName(dto.getServerName());
        character.setCharacterClassName(dto.getCharacterClassName());
        character.setItemAvgLevel(dto.getItemAvgLevel());
        character.setItemMaxLevel(dto.getItemMaxLevel());
        character.setCharacterImage(dto.getCharacterImage());
        character.setExpeditionLevel(dto.getExpeditionLevel() != null ? 
                dto.getExpeditionLevel().toString() : null);
        character.setPvpGradeName(dto.getPvpGradeName());
        character.setGuildName(dto.getGuildName());
        
        try {
            character.setApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            character.setApiResponse(null);
        }
    }
}
