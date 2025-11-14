package com.lostark.backend.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingResponseDto {
    private RankingSummaryDto summary;
    private List<LeaderboardEntryDto> entries;
}
