package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDto {

    @JsonAlias("Cards")
    private List<CardItemDto> cards;

    @JsonAlias("Effects")
    private List<CardSetEffectDto> effects;
}
