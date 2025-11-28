package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardItemDto {

    @JsonAlias("Slot")
    private Integer slot;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("AwakeCount")
    private Integer awakeCount;

    @JsonAlias("AwakeTotal")
    private Integer awakeTotal;

    @JsonAlias("Tooltip")
    private String tooltip;
}
