package com.lostark.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class SkillMenuResponseDto {

    private String characterName;
    private List<CombatSkillDto> combatSkills;
    private List<SkillGemDto> skillGems;
}
