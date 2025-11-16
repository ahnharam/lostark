package com.lostark.backend.service;

import com.lostark.backend.dto.CombatSkillDto;
import com.lostark.backend.dto.SkillGemDto;
import com.lostark.backend.dto.SkillMenuResponseDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    public SkillMenuResponseDto getSkillMenu(String characterName) {
        List<CombatSkillDto> combatSkills = lostArkProfileDomainService.fetchCombatSkills(characterName);
        List<SkillGemDto> skillGems = lostArkProfileDomainService.fetchSkillGems(characterName);

        SkillMenuResponseDto response = new SkillMenuResponseDto();
        response.setCharacterName(characterName);
        response.setCombatSkills(combatSkills);
        response.setSkillGems(skillGems);
        return response;
    }
}
