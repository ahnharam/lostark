package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketOptionsResponse {

    @JsonAlias("Categories")
    private List<MarketCategoryDto> categories;

    @JsonAlias("ItemGrades")
    private List<String> itemGrades;

    @JsonAlias("ItemTiers")
    private List<Integer> itemTiers;

    @JsonAlias("Classes")
    private List<String> classes;
}
