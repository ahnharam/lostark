package com.lostark.backend.dto.market;

import lombok.Data;

@Data
public class MarketItemRefreshRequest {
    private Integer categoryCode;
    private String itemName;
    private String characterClass;
    private Integer itemTier;
    private String itemGrade;
    private String sort = "RECENT_PRICE";
    private String sortCondition = "ASC";
    private Integer pageSize = 10;
}
