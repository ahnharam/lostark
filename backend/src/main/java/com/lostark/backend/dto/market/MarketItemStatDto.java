package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItemStatDto {

    @JsonAlias("AvgPrice")
    private Double avgPrice;

    @JsonAlias("TradeCount")
    private Integer tradeCount;

    @JsonAlias("Date")
    private LocalDate date;
}
