package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItemDto {

    @JsonAlias("Id")
    private Long id;

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Grade")
    private String grade;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("BundleCount")
    private Integer bundleCount;

    @JsonAlias("TradeRemainCount")
    private Integer tradeRemainCount;

    @JsonAlias("YDayAvgPrice")
    private Double yDayAvgPrice;

    @JsonAlias("RecentPrice")
    private Double recentPrice;

    @JsonAlias("CurrentMinPrice")
    private Double currentMinPrice;
}
