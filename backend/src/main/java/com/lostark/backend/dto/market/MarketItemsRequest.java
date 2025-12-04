package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketItemsRequest {

    @JsonAlias("Sort")
    private String sort;

    @JsonAlias("CategoryCode")
    private Integer categoryCode;

    @JsonAlias("CharacterClass")
    private String characterClass;

    @JsonAlias("ItemTier")
    private Integer itemTier;

    @JsonAlias("ItemGrade")
    private String itemGrade;

    @JsonAlias("ItemName")
    private String itemName;

    @JsonAlias("PageNo")
    private Integer pageNo;

    @JsonAlias("PageSize")
    private Integer pageSize;

    @JsonAlias("SortCondition")
    private String sortCondition;
}
