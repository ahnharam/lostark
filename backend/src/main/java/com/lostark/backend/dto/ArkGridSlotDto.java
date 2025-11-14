package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkGridSlotDto {

    @JsonAlias("Index")
    private Integer index;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("Point")
    private Integer point;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("Tooltip")
    private String tooltip;

    @JsonAlias("Gems")
    private List<ArkGridGemDto> gems;
}
