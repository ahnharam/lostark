package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkGridEffectDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Level")
    private Integer level;

    @JsonAlias("Tooltip")
    private String tooltip;
}
