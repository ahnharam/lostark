package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItemDetailDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("BundleCount")
    private Integer bundleCount;

    @JsonAlias("TradeRemainCount")
    private Integer tradeRemainCount;

    @JsonAlias("ToolTip")
    private String toolTip;

    @JsonAlias("Stats")
    private List<MarketItemStatDto> stats;
}
