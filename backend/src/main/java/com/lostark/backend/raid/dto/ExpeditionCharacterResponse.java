package com.lostark.backend.raid.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpeditionCharacterResponse {
    private String characterName;
    private String serverName;
    private String characterClassName;
    private String itemAvgLevel;
    private String itemMaxLevel;
}

