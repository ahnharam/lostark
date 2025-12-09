package com.lostark.backend.market.dto;

import com.lostark.backend.market.entity.MarketDailyStat;
import com.lostark.backend.market.entity.MarketItemAsset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MarketDailyStatDto {
    Long id;
    Long apiItemId;
    Integer categoryCode;
    String itemName;
    String icon;
    Double minPrice;
    Double avgPrice;
    Long tradeCount;
    Long tradeVolume;
    LocalDate statDate;
    LocalDateTime fetchedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static MarketDailyStatDto of(MarketDailyStat stat, MarketItemAsset asset) {
        return MarketDailyStatDto.builder()
                .id(stat.getId())
                .apiItemId(stat.getApiItemId())
                .categoryCode(stat.getCategoryCode())
                .itemName(stat.getItemName() != null ? stat.getItemName() : (asset != null ? asset.getName() : null))
                .icon(asset != null ? asset.getIcon() : null)
                .minPrice(stat.getMinPrice())
                .avgPrice(stat.getAvgPrice())
                .tradeCount(stat.getTradeCount())
                .tradeVolume(stat.getTradeVolume())
                .statDate(stat.getStatDate())
                .fetchedAt(stat.getFetchedAt())
                .createdAt(stat.getCreatedAt())
                .updatedAt(stat.getUpdatedAt())
                .build();
    }
}
