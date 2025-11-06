package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArmoryDto {
    
    @JsonProperty("ArmoryProfile")
    private CharacterProfileDto profile;
    
    @JsonProperty("ArmoryEquipment")
    private List<EquipmentDto> equipment;
    
    @JsonProperty("ArmoryEngraving")
    private EngravingResponseDto engraving;
}
