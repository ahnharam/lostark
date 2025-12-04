package com.lostark.backend.user.service;

import com.lostark.backend.user.dto.UserCreateRequest;
import com.lostark.backend.user.dto.UserResponse;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        AppUser user = new AppUser();
        user.setKakaoId(request.getKakaoId());
        user.setDiscordId(request.getDiscordId());
        user.setKakaoNickname(request.getKakaoNickname());
        user.setDiscordUsername(request.getDiscordUsername());
        user.setMainCharacterName(request.getMainCharacterName());
        
        AppUser saved = appUserRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateDiscordId(Long userId, String discordId, String discordUsername) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user.setDiscordId(discordId);
        user.setDiscordUsername(discordUsername);
        return toResponse(appUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findByDiscordId(String discordId) {
        AppUser user = appUserRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    private UserResponse toResponse(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .discordId(user.getDiscordId())
                .kakaoNickname(user.getKakaoNickname())
                .discordUsername(user.getDiscordUsername())
                .mainCharacterName(user.getMainCharacterName())
                .build();
    }
}
