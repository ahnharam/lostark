package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.EquipmentDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.Equipment;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import com.lostark.backend.repository.CharacterRepository;
import com.lostark.backend.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentService {
    
    private final EquipmentRepository equipmentRepository;
    private final CharacterRepository characterRepository;
    private final LostArkProfileDomainService lostArkProfileDomainService;
    
    @Transactional
    public List<EquipmentDto> getCharacterEquipment(String characterName) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        
        // 캐시된 장비 정보가 있으면 반환
        if (!character.getEquipments().isEmpty()) {
            return character.getEquipments().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        
        // API 호출
        ArmoryDto armory = lostArkProfileDomainService.fetchArmory(characterName);
        if (armory == null || armory.getEquipment() == null) {
            return List.of();
        }
        
        // DB에 저장
        character.getEquipments().clear();
        for (EquipmentDto dto : armory.getEquipment()) {
            Equipment equipment = new Equipment();
            equipment.setCharacter(character);
            equipment.setType(dto.getType());
            equipment.setName(dto.getName());
            equipment.setIcon(dto.getIcon());
            equipment.setGrade(dto.getGrade());
            equipment.setTooltip(dto.getTooltip());
            character.getEquipments().add(equipment);
        }
        
        characterRepository.save(character);
        return armory.getEquipment();
    }
    
    private EquipmentDto convertToDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setType(equipment.getType());
        dto.setName(equipment.getName());
        dto.setIcon(equipment.getIcon());
        dto.setGrade(equipment.getGrade());
        dto.setTooltip(equipment.getTooltip());
        return dto;
    }
}
