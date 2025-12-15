package com.lostark.backend.auth;

import com.lostark.backend.user.dto.UserResponse;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        AppUser user = appUserRepository.findById(principal.getAppUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .discordId(user.getDiscordId())
                .kakaoNickname(user.getKakaoNickname())
                .discordUsername(user.getDiscordUsername())
                .mainCharacterName(user.getMainCharacterName())
                .build());
    }
}

