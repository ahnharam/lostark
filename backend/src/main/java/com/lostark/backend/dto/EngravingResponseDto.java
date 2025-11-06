package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngravingResponseDto {
    
    @JsonProperty(value = "Effects", access = JsonProperty.Access.WRITE_ONLY)
    private List<EngravingEffectDto> effects;
}
