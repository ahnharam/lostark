package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItemsResponse {

    @JsonAlias("PageNo")
    private Integer pageNo;

    @JsonAlias("PageSize")
    private Integer pageSize;

    @JsonAlias("TotalCount")
    private Integer totalCount;

    @JsonAlias("Items")
    private List<MarketItemDto> items;
}
