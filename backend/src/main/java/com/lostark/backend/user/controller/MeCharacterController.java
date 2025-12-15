package com.lostark.backend.user.controller;

import com.lostark.backend.auth.AppUserPrincipal;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.service.CharacterService;
import com.lostark.backend.user.dto.MeCharacterRegisterRequest;
import com.lostark.backend.user.dto.UserResponse;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me/characters")
@RequiredArgsConstructor
public class MeCharacterController {

    private final AppUserRepository appUserRepository;
    private final CharacterService characterService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerMainCharacter(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody MeCharacterRegisterRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (request == null || request.getCharacterName() == null || request.getCharacterName().isBlank()) {
            throw new IllegalArgumentException("캐릭터명을 입력해주세요.");
        }

        String characterName = request.getCharacterName().trim();
        CharacterProfileDto profile = characterService.getCharacterProfile(
                characterName,
                String.valueOf(principal.getAppUserId()),
                request.isForceRefresh()
        );

        AppUser user = appUserRepository.findById(principal.getAppUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setMainCharacterName(profile.getCharacterName());
        AppUser saved = appUserRepository.save(user);

        return ResponseEntity.ok(UserResponse.builder()
                .id(saved.getId())
                .kakaoId(saved.getKakaoId())
                .discordId(saved.getDiscordId())
                .kakaoNickname(saved.getKakaoNickname())
                .discordUsername(saved.getDiscordUsername())
                .mainCharacterName(saved.getMainCharacterName())
                .build());
    }
}
