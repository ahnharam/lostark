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
    public List<EquipmentDto> getCharacterEquipment(String characterName, boolean forceRefresh) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        
        // 캐시된 장비 정보가 있고 강제 새로고침이 아니면 반환
        if (!forceRefresh && !character.getEquipments().isEmpty()) {
            return character.getEquipments().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        
        // API 호출: 전용 equipment 엔드포인트 우선
        List<EquipmentDto> equipmentList = lostArkProfileDomainService.fetchEquipment(characterName);
        if (equipmentList == null || equipmentList.isEmpty()) {
            // 새 데이터가 없으면 기존 캐시를 유지
            if (!character.getEquipments().isEmpty()) {
                return character.getEquipments().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }
            return List.of();
        }
        
        // DB에 저장 (갱신)
        character.getEquipments().clear();
        for (EquipmentDto dto : equipmentList) {
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
        return equipmentList;
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
