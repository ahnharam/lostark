package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterProfileDto {
    
    @JsonProperty("CharacterName")
    private String characterName;
    
    @JsonProperty("ServerName")
    private String serverName;
    
    @JsonProperty("CharacterClassName")
    private String characterClassName;
    
    @JsonProperty("ItemAvgLevel")
    private String itemAvgLevel;
    
    @JsonProperty("ItemMaxLevel")
    private String itemMaxLevel;
    
    @JsonProperty("CharacterImage")
    private String characterImage;
    
    @JsonProperty("ExpeditionLevel")
    private Integer expeditionLevel;
    
    @JsonProperty("PvpGradeName")
    private String pvpGradeName;
    
    @JsonProperty("GuildName")
    private String guildName;
}
