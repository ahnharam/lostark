package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillGemDto {

    @JsonAlias("Slot")
    private Integer slot;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("Level")
    private Integer level;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Tooltip")
    private String tooltip;

    @JsonAlias("Skill")
    private SkillGemSkillDto skill;
}
