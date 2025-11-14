package com.lostark.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileRankingResponseDto {
    private String characterName;
    private String serverName;
    private String characterClassName;
    private RankingMetricDto globalItemLevel;
    private RankingMetricDto globalClassItemLevel;
    private RankingMetricDto serverItemLevel;
    private RankingMetricDto serverClassItemLevel;
    private RankingMetricDto expeditionLevel;
    private RankingMetricDto collectionScore;
}
