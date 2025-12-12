package com.lostark.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/proxy")
public class ImageProxyController {

    private final RestTemplate restTemplate;

    public ImageProxyController(@Autowired(required = false) RestTemplate restTemplate) {
        if (restTemplate != null) {
            this.restTemplate = restTemplate;
            log.info("ImageProxyController initialized with injected RestTemplate");
        } else {
            this.restTemplate = createRestTemplate();
            log.info("ImageProxyController initialized with custom RestTemplate");
        }
    }

    private RestTemplate createRestTemplate() {
        try {
            // SSL 인증서 무시 설정 (개발 환경용)
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);

            return new RestTemplate(factory);
        } catch (Exception e) {
            log.error("Failed to create custom RestTemplate", e);
            return new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(10))
                    .setReadTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Proxy controller is working!");
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            // URL 유효성 검사
            if (url == null || url.trim().isEmpty()) {
                log.warn("Empty image URL provided");
                return ResponseEntity.badRequest().build();
            }

            // https로 정규화
            String normalizedUrl = normalizeUrl(url);
            log.info("Proxy image request url={} normalized={}", url, normalizedUrl);
            
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
            headers.set("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            headers.set("Referer", "https://lostark.game.onstove.com");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 이미지 요청
            ResponseEntity<byte[]> response = restTemplate.exchange(
                URI.create(normalizedUrl),
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getBody() == null || response.getBody().length == 0) {
                log.warn("Empty image response url={} status={}", normalizedUrl, response.getStatusCode());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            // 응답 헤더 설정 (CORS 헤더 제거 - WebConfig에서 처리)
            HttpHeaders responseHeaders = new HttpHeaders();

            MediaType contentType = response.getHeaders().getContentType();
            // Content-Type 설정
            if (contentType != null) {
                responseHeaders.setContentType(contentType);
            } else {
                // URL에서 확장자 추출하여 Content-Type 설정
                contentType = MediaType.parseMediaType(guessContentType(normalizedUrl));
                responseHeaders.setContentType(contentType);
            }
            
            // 캐시 설정
            responseHeaders.setCacheControl(CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS));

            log.info(
                "Proxy image success url={} status={} size={} contentType={}",
                normalizedUrl,
                response.getStatusCode(),
                response.getBody().length,
                contentType
            );
            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
            
        } catch (RestClientException e) {
            log.error("RestClient error while proxying image url={}", url, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid image URL url={}", url, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error while proxying image url={}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        
        String normalized = url.trim();
        
        // //로 시작하는 경우
        if (normalized.startsWith("//")) {
            normalized = "https:" + normalized;
        }
        // http://를 https://로 변경
        else if (normalized.startsWith("http://")) {
            normalized = normalized.replace("http://", "https://");
        }
        
        return normalized;
    }

    private String guessContentType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".png")) return "image/png";
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) return "image/jpeg";
        if (lowerUrl.endsWith(".gif")) return "image/gif";
        if (lowerUrl.endsWith(".webp")) return "image/webp";
        if (lowerUrl.endsWith(".svg")) return "image/svg+xml";
        return "image/png"; // 기본값
    }
}
