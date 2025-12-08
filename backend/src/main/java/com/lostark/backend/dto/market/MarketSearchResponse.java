package com.lostark.backend.dto.market;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketSearchResponse {
    private Integer categoryCode;
    private String characterClass;
    private String sort;
    private String sortCondition;
    private Integer itemTier;
    private String itemGrade;
    private String itemName;
    private Integer page;
    private Integer pageSize;
    private Integer totalCount;
    private Integer totalPages;
    private Map<Integer, List<MarketItemDto>> pages;
    private Long fetchedAt;
}
