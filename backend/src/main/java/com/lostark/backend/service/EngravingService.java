package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.EngravingEffectDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.Engraving;
import com.lostark.backend.repository.CharacterRepository;
import com.lostark.backend.repository.EngravingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EngravingService {
    
    private final EngravingRepository engravingRepository;
    private final CharacterRepository characterRepository;
    private final LostArkApiService lostArkApiService;
    
    @Transactional
    public List<EngravingEffectDto> getCharacterEngravings(String characterName) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        
        // 캐시된 각인 정보가 있으면 반환
        if (!character.getEngravings().isEmpty()) {
            return character.getEngravings().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        
        // API 호출
        ArmoryDto armory = lostArkApiService.getCharacterArmory(characterName).block();
        if (armory == null || armory.getEngraving() == null || armory.getEngraving().getEffects() == null) {
            return List.of();
        }
        
        // DB에 저장
        character.getEngravings().clear();
        for (EngravingEffectDto dto : armory.getEngraving().getEffects()) {
            Engraving engraving = new Engraving();
            engraving.setCharacter(character);
            engraving.setName(dto.getName());
            engraving.setIcon(dto.getIcon());
            engraving.setTooltip(dto.getDescription());
            character.getEngravings().add(engraving);
        }
        
        characterRepository.save(character);
        return armory.getEngraving().getEffects();
    }
    
    private EngravingEffectDto convertToDto(Engraving engraving) {
        EngravingEffectDto dto = new EngravingEffectDto();
        dto.setName(engraving.getName());
        dto.setIcon(engraving.getIcon());
        dto.setDescription(engraving.getTooltip());
        return dto;
    }
}
