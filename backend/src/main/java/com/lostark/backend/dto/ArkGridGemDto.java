package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkGridGemDto {

    @JsonAlias("Index")
    private Integer index;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("IsActive")
    private Boolean isActive;

    @JsonAlias("Tooltip")
    private String tooltip;
}
