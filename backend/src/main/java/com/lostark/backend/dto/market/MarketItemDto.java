package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("yDayAvgPrice")
    @JsonAlias({ "YDayAvgPrice", "yDayAvgPrice", "ydayAvgPrice", "y_day_avg_price", "YDAY_AVG_PRICE" })
    private Double yDayAvgPrice;

    @JsonProperty("recentPrice")
    @JsonAlias({ "RecentPrice", "recentPrice", "recent_price" })
    private Double recentPrice;

    @JsonProperty("currentMinPrice")
    @JsonAlias({ "CurrentMinPrice", "currentMinPrice", "current_min_price" })
    private Double currentMinPrice;
}
