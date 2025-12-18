package com.lostark.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class CsrfController {

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token, HttpServletRequest request) {
        log.debug(
                "CSRF token requested: remoteAddr={}, hasSession={}, userAgent={}",
                request.getRemoteAddr(),
                request.getSession(false) != null,
                request.getHeader("User-Agent")
        );
        return Map.of("token", token.getToken());
    }
}
