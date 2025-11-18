package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectibleDto {

    @JsonAlias("CollectibleId")
    private Integer collectibleId;

    @JsonAlias("CollectibleLevel")
    private Integer collectibleLevel;

    @JsonAlias("Point")
    private Integer point;

    @JsonAlias("MaxPoint")
    private Integer maxPoint;

    @JsonAlias("Type")
    private String type;

    @JsonAlias("Icon")
    private String icon;

    @JsonAlias("CollectiblePoints")
    private List<CollectiblePointDto> collectiblePoints;
}
