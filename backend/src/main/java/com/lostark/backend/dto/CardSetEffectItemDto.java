package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSetEffectItemDto {

    @JsonAlias("Name")
    private String name;

    @JsonAlias("Description")
    private String description;
}
