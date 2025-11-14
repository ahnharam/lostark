package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkPassivePointDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Value")
    private Integer value;

    @JsonAlias("Description")
    private String description;

    @JsonAlias("Tooltip")
    private String tooltip;
}
