package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipmentDto {
    
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Icon")
    private String icon;
    
    @JsonProperty("Grade")
    private String grade;
    
    @JsonProperty("Tooltip")
    private String tooltip;
}
