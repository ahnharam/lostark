package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngravingResponseDto {

    @JsonAlias("Effects")
    private List<EngravingEffectDto> effects;

    @JsonAlias("ArkPassiveEffects")
    private List<EngravingEffectDto> arkPassiveEffects;
}
