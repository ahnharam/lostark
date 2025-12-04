package com.lostark.backend.user.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String kakaoId;
    private String discordId;
    private String kakaoNickname;
    private String discordUsername;
    private String mainCharacterName;
}
