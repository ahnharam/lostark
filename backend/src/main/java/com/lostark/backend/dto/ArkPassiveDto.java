package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkPassiveDto {

    @JsonAlias("Title")
    private String title;

    @JsonAlias("IsArkPassive")
    private boolean isArkPassive;

    @JsonAlias("Points")
    private List<ArkPassivePointDto> points;

    @JsonAlias("Effects")
    private List<ArkPassiveEffectDto> effects;
}
