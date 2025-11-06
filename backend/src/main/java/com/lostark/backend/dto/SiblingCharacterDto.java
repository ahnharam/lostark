package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiblingCharacterDto {
    
    @JsonProperty(value = "CharacterName", access = JsonProperty.Access.WRITE_ONLY)
    private String characterName;
    
    @JsonProperty(value = "ServerName", access = JsonProperty.Access.WRITE_ONLY)
    private String serverName;
    
    @JsonProperty(value = "CharacterClassName", access = JsonProperty.Access.WRITE_ONLY)
    private String characterClassName;
    
    @JsonProperty(value = "ItemAvgLevel", access = JsonProperty.Access.WRITE_ONLY)
    private String itemAvgLevel;
    
    @JsonProperty(value = "ItemMaxLevel", access = JsonProperty.Access.WRITE_ONLY)
    private String itemMaxLevel;
}
