package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketOptionsResponse {

    @JsonAlias("Categories")
    @JsonProperty("Categories")
    private List<MarketCategoryDto> categories;

    @JsonAlias("ItemGrades")
    @JsonProperty("ItemGrades")
    private List<String> itemGrades;

    @JsonAlias("ItemTiers")
    @JsonProperty("ItemTiers")
    private List<Integer> itemTiers;

    @JsonAlias("Classes")
    @JsonProperty("Classes")
    private List<String> classes;
}
