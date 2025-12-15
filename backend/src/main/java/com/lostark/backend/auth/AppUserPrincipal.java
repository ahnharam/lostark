package com.lostark.backend.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class AppUserPrincipal implements OAuth2User, Serializable {

    private final Long appUserId;
    private final String discordId;
    private final String discordUsername;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(
            Long appUserId,
            String discordId,
            String discordUsername,
            Map<String, Object> attributes,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.appUserId = appUserId;
        this.discordId = discordId;
        this.discordUsername = discordUsername;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    public Long getAppUserId() {
        return appUserId;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        if (discordUsername != null && !discordUsername.isBlank()) return discordUsername;
        return discordId != null ? discordId : String.valueOf(appUserId);
    }
}

