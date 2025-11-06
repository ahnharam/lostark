package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArmoryDto {

    @JsonAlias("ArmoryProfile")
    private CharacterProfileDto profile;

    @JsonAlias("ArmoryEquipment")
    private List<EquipmentDto> equipment;

    @JsonAlias("ArmoryEngraving")
    private EngravingResponseDto engraving;
}
