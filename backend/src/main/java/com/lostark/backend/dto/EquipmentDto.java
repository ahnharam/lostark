package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipmentDto {

    @JsonAlias("Type")
    private String type;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("Tooltip")
    private String tooltip;
}
