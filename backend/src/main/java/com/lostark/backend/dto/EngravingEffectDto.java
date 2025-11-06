package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngravingEffectDto {
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Icon")
    private String icon;
    
    @JsonProperty("Description")
    private String description;
}
