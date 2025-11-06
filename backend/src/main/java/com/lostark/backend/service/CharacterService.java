package com.lostark.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    private final LostArkApiService lostArkApiService;
    private final ObjectMapper objectMapper;
    
    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    
    @Transactional
    public CharacterProfileDto getCharacterProfile(String characterName) {
        // 1. DB에서 캐시 확인
        Optional<Character> cachedCharacter = characterRepository.findByCharacterName(characterName);
        
        if (cachedCharacter.isPresent()) {
            Character character = cachedCharacter.get();
            // 캐시가 1시간 이내면 DB 데이터 반환
            if (Duration.between(character.getUpdatedAt(), LocalDateTime.now()).compareTo(CACHE_DURATION) < 0) {
                return convertToDto(character);
            }
        }
        
        // 2. API 호출
        CharacterProfileDto profile = lostArkApiService.getCharacterProfile(characterName).block();
        
        if (profile == null) {
            throw new RuntimeException("캐릭터를 찾을 수 없습니다.");
        }
        
        // 3. DB에 저장 또는 업데이트
        Character character = cachedCharacter.orElse(new Character());
        updateCharacterFromDto(character, profile);
        characterRepository.save(character);
        
        return profile;
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
