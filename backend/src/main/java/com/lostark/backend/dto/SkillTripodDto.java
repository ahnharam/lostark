package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillTripodDto {

    @JsonAlias("Tier")
    private Integer tier;

    @JsonAlias("Slot")
    private Integer slot;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Level")
    private Integer level;

    @JsonAlias({"IsSelected", "isSelected"})
    private Boolean selected;

    @JsonAlias("Tooltip")
    private String tooltip;
}
