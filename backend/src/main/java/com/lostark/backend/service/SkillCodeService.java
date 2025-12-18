package com.lostark.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.lostark.util.LostArkSkillCodeParser;
import com.lostark.backend.lostark.util.LostArkSkillCodeParser.ProfileIdentifiers;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCodeService {

    private static final String LOSTARK_WEB_BASE = "https://lostark.game.onstove.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public String getSkillCode(String characterName) {
        String profileHtml = fetchProfileHtml(characterName);
        ProfileIdentifiers identifiers = LostArkSkillCodeParser
                .parseProfileIdentifiers(profileHtml)
                .orElseThrow(() -> new ApiException("프로필에서 식별자(memberNo/worldNo/pcId)를 찾지 못했습니다."));

        String skillRecommendJson = fetchSkillRecommend(identifiers, characterName);
        String contentHtml = extractContentHtml(skillRecommendJson);

        return LostArkSkillCodeParser
                .extractSkillCodeFromContent(contentHtml)
                .orElseThrow(() -> new ApiException("SkillRecommend 응답에서 스킬 코드를 찾지 못했습니다."));
    }

    private String fetchProfileHtml(String characterName) {
        URI url = UriComponentsBuilder
                .fromHttpUrl(LOSTARK_WEB_BASE)
                .path("/Profile/Character/{characterName}")
                .buildAndExpand(characterName)
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .blockOptional()
                    .orElseThrow(() -> new ApiException("프로필 HTML을 가져오지 못했습니다."));
        } catch (WebClientResponseException.NotFound e) {
            throw new CharacterNotFoundException(characterName, e);
        } catch (WebClientResponseException e) {
            throw new ApiException("프로필 HTML 요청이 실패했습니다. status=" + e.getStatusCode().value(), e);
        } catch (Exception e) {
            throw new ApiException("프로필 HTML을 가져오는 중 오류가 발생했습니다.", e);
        }
    }

    private String fetchSkillRecommend(ProfileIdentifiers identifiers, String characterName) {
        String url = LOSTARK_WEB_BASE + "/Profile/SkillRecommend";
        String referer = UriComponentsBuilder
                .fromHttpUrl(LOSTARK_WEB_BASE)
                .path("/Profile/Character/{characterName}")
                .buildAndExpand(characterName)
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        try {
            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + ", */*")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header(HttpHeaders.ORIGIN, LOSTARK_WEB_BASE)
                    .header(HttpHeaders.REFERER, referer)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(BodyInserters.fromFormData("memberNo", identifiers.memberNo())
                            .with("worldNo", identifiers.worldNo())
                            .with("pcId", identifiers.pcId()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .blockOptional()
                    .orElseThrow(() -> new ApiException("SkillRecommend 응답을 가져오지 못했습니다."));
        } catch (WebClientResponseException e) {
            throw new ApiException("SkillRecommend 요청이 실패했습니다. status=" + e.getStatusCode().value(), e);
        } catch (Exception e) {
            throw new ApiException("SkillRecommend 호출 중 오류가 발생했습니다.", e);
        }
    }

    private String extractContentHtml(String skillRecommendJson) {
        try {
            JsonNode root = objectMapper.readTree(skillRecommendJson);
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                String title = root.path("title").asText("");
                throw new ApiException("SkillRecommend 오류(code=" + code + (title.isBlank() ? "" : ", title=" + title) + ")");
            }
            String content = root.path("content").asText("");
            if (content.isBlank()) {
                throw new ApiException("SkillRecommend 응답 content가 비어있습니다.");
            }
            return content;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("SkillRecommend JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
}

