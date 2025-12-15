package com.lostark.backend.friend.controller;

import com.lostark.backend.auth.AppUserPrincipal;
import com.lostark.backend.friend.dto.*;
import com.lostark.backend.friend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<FriendResponse>> myFriends(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(friendshipService.getMyFriends(principal.getAppUserId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<FriendRequestsResponse> myRequests(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(friendshipService.getMyRequests(principal.getAppUserId()));
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendRequestResponse> createRequest(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody FriendRequestCreateRequest request
    ) {
        return ResponseEntity.ok(friendshipService.createFriendRequest(principal.getAppUserId(), request.getDiscordUserId()));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> accept(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(friendshipService.acceptRequest(principal.getAppUserId(), requestId));
    }

    @PostMapping("/requests/{requestId}/decline")
    public ResponseEntity<FriendRequestResponse> decline(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(friendshipService.declineRequest(principal.getAppUserId(), requestId));
    }

    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        friendshipService.cancelRequest(principal.getAppUserId(), requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long friendshipId
    ) {
        friendshipService.removeFriend(principal.getAppUserId(), friendshipId);
        return ResponseEntity.ok().build();
    }
}

