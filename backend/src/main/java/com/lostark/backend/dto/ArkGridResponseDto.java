package com.lostark.backend.dto;

import lombok.Data;

@Data
public class ArkGridResponseDto {
    private String characterName;
    private ArkPassiveDto arkPassive;
    private ArkGridDto arkGrid;
}
