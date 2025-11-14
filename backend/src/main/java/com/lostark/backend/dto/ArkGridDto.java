package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArkGridDto {

    @JsonAlias("Slots")
    private List<ArkGridSlotDto> slots;

    @JsonAlias("Effects")
    private List<ArkGridEffectDto> effects;
}
