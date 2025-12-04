package com.lostark.backend.dto.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketCategoryDto {

    @JsonAlias("Code")
    private Integer code;

    @JsonAlias("CodeName")
    private String codeName;

    @JsonAlias({"Subs", "Subcategories"})
    private List<MarketCategoryDto> subs;
}
