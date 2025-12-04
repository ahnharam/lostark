package com.lostark.backend.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String kakaoId;
    private String discordId;
    private String kakaoNickname;
    private String discordUsername;
    private String mainCharacterName;
}
