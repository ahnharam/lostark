package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterProfileDto {

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

    @JsonProperty(value = "CharacterImage", access = JsonProperty.Access.WRITE_ONLY)
    private String characterImage;

    @JsonProperty(value = "ExpeditionLevel", access = JsonProperty.Access.WRITE_ONLY)
    private Integer expeditionLevel;

    @JsonProperty(value = "PvpGradeName", access = JsonProperty.Access.WRITE_ONLY)
    private String pvpGradeName;

    @JsonProperty(value = "GuildName", access = JsonProperty.Access.WRITE_ONLY)
    private String guildName;
}
