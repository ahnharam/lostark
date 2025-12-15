package com.lostark.backend.auth;

import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AppUserRepository appUserRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"discord".equals(registrationId)) {
            return oauth2User;
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        String discordId = asString(attributes.get("id"));
        String username = asString(attributes.get("username"));
        String globalName = asString(attributes.get("global_name"));

        if (discordId == null || discordId.isBlank()) {
            throw new OAuth2AuthenticationException("Discord user id is missing");
        }

        String displayName = (globalName != null && !globalName.isBlank()) ? globalName : username;

        AppUser user = appUserRepository.findByDiscordId(discordId).orElseGet(AppUser::new);
        user.setDiscordId(discordId);
        user.setDiscordUsername(displayName);
        AppUser saved = appUserRepository.save(user);

        return new AppUserPrincipal(
                saved.getId(),
                discordId,
                displayName,
                attributes,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private String asString(Object value) {
        if (value == null) return null;
        return String.valueOf(value);
    }
}

