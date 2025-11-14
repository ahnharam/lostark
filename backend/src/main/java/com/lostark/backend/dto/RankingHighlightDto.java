package com.lostark.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingHighlightDto {
    private String characterName;
    private String serverName;
    private String characterClassName;
    private Integer rank;
    private Integer rating;
    private String itemAvgLevel;
    private Integer tier;
    private Integer division;
}
