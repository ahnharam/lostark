package com.lostark.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingSummaryDto {
    private String leaderboardCode;
    private String seasonId;
    private int page;
    private int totalFetched;
    private String lastUpdated;
    private RankingHighlightDto highlightedCharacter;
}
