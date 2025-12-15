package com.lostark.backend.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendUserResponse {
    private Long userId;
    private String discordUserId;
    private String discordUsername;
}

