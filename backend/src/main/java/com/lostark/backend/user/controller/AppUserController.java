package com.lostark.backend.user.controller;

import com.lostark.backend.user.dto.UserCreateRequest;
import com.lostark.backend.user.dto.UserResponse;
import com.lostark.backend.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    /**
     * 유저 생성
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        UserResponse response = appUserService.createUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = appUserService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 유저 목록
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = appUserService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * Discord 연동
     */
    @PatchMapping("/{userId}/discord")
    public ResponseEntity<UserResponse> linkDiscord(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        String discordId = request.get("discordId");
        String discordUsername = request.get("discordUsername");
        UserResponse response = appUserService.updateDiscordId(userId, discordId, discordUsername);
        return ResponseEntity.ok(response);
    }
}
