package com.lostark.backend.friend.dto;

import com.lostark.backend.friend.entity.FriendshipStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendRequestResponse {
    private Long requestId;
    private FriendUserResponse requester;
    private FriendUserResponse addressee;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}

