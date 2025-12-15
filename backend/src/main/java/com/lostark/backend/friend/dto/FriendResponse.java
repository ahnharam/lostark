package com.lostark.backend.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponse {
    private Long friendshipId;
    private FriendUserResponse friend;
}

