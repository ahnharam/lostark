package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterProfileDto {

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

    @JsonAlias("CharacterImage")
    private String characterImage;

    @JsonAlias("ExpeditionLevel")
    private Integer expeditionLevel;

    @JsonAlias("PvpGradeName")
    private String pvpGradeName;

    @JsonAlias("GuildName")
    private String guildName;
}
