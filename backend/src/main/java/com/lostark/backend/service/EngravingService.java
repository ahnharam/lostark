package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.EngravingEffectDto;
import com.lostark.backend.dto.EngravingResponseDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.Engraving;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import com.lostark.backend.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class EngravingService {

    private final CharacterRepository characterRepository;
    private final LostArkProfileDomainService lostArkProfileDomainService;

    @Transactional
    public List<EngravingEffectDto> getCharacterEngravings(String characterName) {
        try {
            Character character = characterRepository.findByCharacterName(characterName).orElse(null);

            if (character != null && !character.getEngravings().isEmpty()) {
                return character.getEngravings().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }

            List<EngravingEffectDto> effects = fetchEngravingEffects(characterName);
            if (effects == null || effects.isEmpty()) {
                return List.of();
            }

            if (character != null) {
                character.getEngravings().clear();
                for (EngravingEffectDto dto : effects) {
                    Engraving engraving = new Engraving();
                    engraving.setCharacter(character);
                    engraving.setName(dto.getName());
                    engraving.setIcon(dto.getIcon());
                    engraving.setLevel(dto.getLevel());
                    engraving.setGrade(dto.getGrade());
                    engraving.setAbilityStoneLevel(dto.getAbilityStoneLevel());
                    engraving.setTooltip(dto.getDescription());
                    character.getEngravings().add(engraving);
                }
                characterRepository.save(character);
            }

            return effects;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("각인 정보 조회 실패: {} - {}", characterName, e.getMessage(), e);
            throw new ApiException("각인 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    private List<EngravingEffectDto> fetchEngravingEffects(String characterName) {
        EngravingResponseDto response = lostArkProfileDomainService.fetchEngravings(characterName);
        List<EngravingEffectDto> effects = extractEffects(response);

        if (effects == null || effects.isEmpty()) {
            ArmoryDto armory = lostArkProfileDomainService.fetchArmory(characterName);
            if (armory != null && armory.getEngraving() != null) {
                effects = extractEffects(armory.getEngraving());
            }
        }
        return effects;
    }

    private List<EngravingEffectDto> extractEffects(EngravingResponseDto response) {
        if (response == null) {
            return null;
        }
        if (response.getEffects() != null && !response.getEffects().isEmpty()) {
            return response.getEffects();
        }
        return response.getArkPassiveEffects();
    }

    private EngravingEffectDto convertToDto(Engraving engraving) {
        EngravingEffectDto dto = new EngravingEffectDto();
        dto.setName(engraving.getName());
        dto.setIcon(engraving.getIcon());
        dto.setLevel(engraving.getLevel());
        dto.setGrade(engraving.getGrade());
        dto.setAbilityStoneLevel(engraving.getAbilityStoneLevel());
        dto.setDescription(engraving.getTooltip());
        return dto;
    }
}
