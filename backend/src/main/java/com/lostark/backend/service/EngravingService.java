package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.EngravingEffectDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.Engraving;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.repository.CharacterRepository;
import com.lostark.backend.repository.EngravingRepository;
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

    private final EngravingRepository engravingRepository;
    private final CharacterRepository characterRepository;
    private final LostArkApiService lostArkApiService;

    @Transactional
    public List<EngravingEffectDto> getCharacterEngravings(String characterName) {
        try {
            // API 호출
            ArmoryDto armory = lostArkApiService.getCharacterArmory(characterName).block();
            if (armory == null || armory.getEngraving() == null || armory.getEngraving().getEffects() == null) {
                return List.of();
            }

            // DB에서 캐릭터 찾기 (없으면 캐시 없이 API 결과만 반환)
            Character character = characterRepository.findByCharacterName(characterName).orElse(null);

            if (character != null) {
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
            }

            return armory.getEngraving().getEffects();
        } catch (Exception e) {
            log.error("각인 정보 조회 실패: {} - {}", characterName, e.getMessage(), e);
            throw new ApiException("각인 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    private EngravingEffectDto convertToDto(Engraving engraving) {
        EngravingEffectDto dto = new EngravingEffectDto();
        dto.setName(engraving.getName());
        dto.setIcon(engraving.getIcon());
        dto.setDescription(engraving.getTooltip());
        return dto;
    }
}
