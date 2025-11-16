package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillRuneDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Tooltip")
    private String tooltip;
}
