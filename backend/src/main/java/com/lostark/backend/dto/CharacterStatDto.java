package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterStatDto {
    
    @JsonAlias("Type")
    private String type;
    
    @JsonAlias("Value")
    private Object value;
    
    @JsonAlias("Tooltip")
    private Object tooltip;
}
