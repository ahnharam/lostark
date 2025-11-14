package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaderboardEntryDto {

    @JsonAlias("Rank")
    private Integer rank;

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

    @JsonAlias("Rating")
    private Integer rating;

    @JsonAlias("Score")
    private Integer score;

    @JsonAlias("Tier")
    private Integer tier;

    @JsonAlias("Division")
    private Integer division;

    @JsonAlias("SeasonId")
    private String seasonId;

    @JsonAlias("SeasonName")
    private String seasonName;

    @JsonAlias("GuildName")
    private String guildName;

    @JsonAlias("UpdatedDate")
    private String updatedDate;
}
