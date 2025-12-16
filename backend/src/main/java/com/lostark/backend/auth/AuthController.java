package com.lostark.backend.auth;

import com.lostark.backend.user.dto.UserResponse;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;

    @Value("${app.security.dev-login.enabled:false}")
    private boolean devLoginEnabled;

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

    @PostMapping("/dev-login")
    public ResponseEntity<UserResponse> devLogin(@RequestBody DevLoginRequest request, HttpServletRequest httpRequest) {
        if (!devLoginEnabled) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        String discordUserId = request.discordUserId();
        if (discordUserId == null || discordUserId.isBlank()) {
            throw new IllegalArgumentException("discordUserId가 필요합니다.");
        }

        AppUser user = appUserRepository.findByDiscordId(discordUserId)
                .orElseGet(() -> {
                    AppUser created = new AppUser();
                    created.setDiscordId(discordUserId);
                    created.setDiscordUsername("User#" + discordUserId);
                    return appUserRepository.save(created);
                });

        AppUserPrincipal principal = new AppUserPrincipal(
                user.getId(),
                user.getDiscordId(),
                user.getDiscordUsername(),
                Map.of("id", user.getDiscordId(), "username", user.getDiscordUsername()),
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .discordId(user.getDiscordId())
                .kakaoNickname(user.getKakaoNickname())
                .discordUsername(user.getDiscordUsername())
                .mainCharacterName(user.getMainCharacterName())
                .build());
    }

    public record DevLoginRequest(String discordUserId) {}
}
