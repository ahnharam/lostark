package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngravingEffectDto {

    @JsonProperty(value = "Name", access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    @JsonProperty(value = "Icon", access = JsonProperty.Access.WRITE_ONLY)
    private String icon;

    @JsonProperty(value = "Description", access = JsonProperty.Access.WRITE_ONLY)
    private String description;
}
