package com.lostark.backend.dto.market;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketSyncResultDto {
    private int categoriesProcessed;
    private long itemsSaved;
    private int pagesFetched;
}
