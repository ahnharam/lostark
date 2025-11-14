package com.lostark.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingMetricDto {
    private Integer rank;
    private Integer total;
    private Double metricValue;
    private String unit;
    private Double percentile;
}
