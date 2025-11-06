package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiblingCharacterDto {
    
    @JsonAlias("CharacterName")
    private String characterName;
    
    @JsonAlias("ServerName")
    private String serverName;
    
    @JsonAlias("CharacterClassName")
    private String characterClassName;
    
    @JsonAlias("ItemAvgLevel")
    private String itemAvgLevel;
    
    @JsonAlias("ItemMaxLevel")
    private String itemMaxLevel;
}
