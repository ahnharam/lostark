package com.lostark.backend.config.security;

import com.lostark.backend.auth.DiscordOAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8081}")
    private String allowedOrigins;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.frontend.success-redirect-path:/raid}")
    private String successRedirectPath;

    @Value("${app.security.csrf.cookie.same-site:${SESSION_COOKIE_SAME_SITE:Lax}}")
    private String csrfCookieSameSite;

    @Value("${app.security.csrf.cookie.secure:${SESSION_COOKIE_SECURE:false}}")
    private boolean csrfCookieSecure;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DiscordOAuth2UserService discordOAuth2UserService)
            throws Exception {

        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        csrfTokenRepository.setSecure(csrfCookieSecure);
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.sameSite(csrfCookieSameSite));

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        // SPA에서 cookie 값을 그대로 헤더로 올리는 방식(Angular 등)과 호환되도록 raw 토큰 처리
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/me/**")
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (accessDeniedException instanceof CsrfException) {
                                log.warn(
                                        "CSRF denied: method={} uri={} origin={} referer={} hasXsrfCookie={} hasXsrfHeader={} userAgent={}",
                                        request.getMethod(),
                                        request.getRequestURI(),
                                        request.getHeader("Origin"),
                                        request.getHeader("Referer"),
                                        hasCookie(request, "XSRF-TOKEN"),
                                        request.getHeader("X-XSRF-TOKEN") != null,
                                        request.getHeader("User-Agent")
                                );
                            }
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/markets/**").permitAll()
                        .requestMatchers("/api/oauth2/**", "/api/login/**").permitAll()
                        .requestMatchers("/api/me/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/login/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(discordOAuth2UserService))
                        .defaultSuccessUrl(buildFrontendSuccessUrl(), true)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseAllowedOrigins(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    private List<String> parseAllowedOrigins(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String buildFrontendSuccessUrl() {
        String base = frontendBaseUrl != null ? frontendBaseUrl.trim() : "";
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String path = successRedirectPath != null ? successRedirectPath.trim() : "/";
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }

    private boolean hasCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return false;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) return true;
        }
        return false;
    }

}
