package com.lostark.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeCharacterRegisterRequest {
    private String characterName;
    private boolean forceRefresh;
}

