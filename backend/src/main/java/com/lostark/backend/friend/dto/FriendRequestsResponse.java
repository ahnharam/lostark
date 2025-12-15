package com.lostark.backend.friend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FriendRequestsResponse {
    private List<FriendRequestResponse> outgoing;
    private List<FriendRequestResponse> incoming;
}

