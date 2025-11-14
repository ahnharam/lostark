package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkPassiveEffectDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Description")
    private String description;

    @JsonAlias("ToolTip")
    private String toolTip;
}
