package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectiblePointDto {

    @JsonAlias("PointName")
    private String pointName;

    @JsonAlias("Point")
    private Integer point;

    @JsonAlias("MaxPoint")
    private Integer maxPoint;
}
