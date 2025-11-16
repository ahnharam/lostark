package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CombatSkillDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Type")
    private String type;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Level")
    private Integer level;

    @JsonAlias("SkillType")
    private String skillType;

    @JsonAlias("SkillPoints")
    private Integer skillPoints;

    @JsonAlias("Tooltip")
    private String tooltip;

    @JsonAlias("Tripods")
    private List<SkillTripodDto> tripods;

    @JsonAlias("Rune")
    private SkillRuneDto rune;
}
