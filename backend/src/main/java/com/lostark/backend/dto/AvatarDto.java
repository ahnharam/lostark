package com.lostark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvatarDto {
    private String Type;
    private String Name;
    private String Icon;
    private String Grade;
    private Boolean IsSet;
    private Boolean IsInner;
    private String Tooltip;
}
