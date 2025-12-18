package com.lostark.backend.config.security;

import com.lostark.backend.auth.CsrfController;
import com.lostark.backend.auth.DiscordOAuth2UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CsrfController.class, TestMutationController.class})
@Import(SecurityConfig.class)
class CsrfSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiscordOAuth2UserService discordOAuth2UserService;

    @Test
    void postWithoutCsrfReturnsForbidden() throws Exception {
        mvc.perform(post("/api/test/mutate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postWithCsrfCookieAndHeaderSucceeds() throws Exception {
        MvcResult result = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        Cookie xsrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        if (xsrfCookie == null) {
            throw new IllegalStateException("missing XSRF-TOKEN cookie");
        }
        String token = extractToken(result.getResponse().getContentAsString());

        mvc.perform(
                        post("/api/test/mutate")
                                .cookie(xsrfCookie)
                                .header("X-XSRF-TOKEN", token)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    private String extractToken(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        JsonNode token = node.get("token");
        if (token == null || token.isNull()) {
            throw new IllegalStateException("missing token");
        }
        return token.asText();
    }
}
