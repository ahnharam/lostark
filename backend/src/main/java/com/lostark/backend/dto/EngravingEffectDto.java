package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngravingEffectDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Description")
    private String description;
}
