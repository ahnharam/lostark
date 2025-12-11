package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvatarDto {
    @JsonAlias("Type")
    private String type;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("IsSet")
    private Boolean isSet;

    @JsonAlias("IsInner")
    private Boolean isInner;

    @JsonAlias("Tooltip")
    private String tooltip;
}
