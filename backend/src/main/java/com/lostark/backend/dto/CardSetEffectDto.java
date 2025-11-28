package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSetEffectDto {

    @JsonAlias("Index")
    private Integer index;

    @JsonAlias("CardSlots")
    private List<Integer> cardSlots;

    @JsonAlias("Items")
    private List<CardSetEffectItemDto> items;
}
