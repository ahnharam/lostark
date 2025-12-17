package com.lostark.backend.config.security;

import com.lostark.backend.auth.CsrfController;
import com.lostark.backend.auth.DiscordOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CsrfController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "SESSION_COOKIE_SAME_SITE=None",
        "SESSION_COOKIE_SECURE=true"
})
class CsrfCookieSettingsTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DiscordOAuth2UserService discordOAuth2UserService;

    @Test
    void csrfCookieInheritsSessionCookieSettingsWhenNotOverridden() throws Exception {
        MvcResult result = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders("Set-Cookie");
        assertThat(setCookies).anyMatch(value ->
                value.contains("XSRF-TOKEN=") &&
                        value.contains("SameSite=None") &&
                        value.contains("Secure")
        );
    }
}

