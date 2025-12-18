package com.lostark.backend.controller;

import com.lostark.backend.dto.SkillCodeResponseDto;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.exception.ErrorResponse;
import com.lostark.backend.service.SkillCodeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/skill-codes")
@RequiredArgsConstructor
public class SkillCodeController {

    private final SkillCodeService skillCodeService;

    @GetMapping("/{characterName}")
    public ResponseEntity<?> getSkillCode(@PathVariable String characterName, HttpServletRequest request) {
        try {
            String skillCode = skillCodeService.getSkillCode(characterName);
            SkillCodeResponseDto response = new SkillCodeResponseDto();
            response.setCharacterName(characterName);
            response.setSkillCode(skillCode);
            return ResponseEntity.ok(response);
        } catch (CharacterNotFoundException e) {
            return ResponseEntity.status(404).body(
                    ErrorResponse.of(404, "Character Not Found", e.getMessage(), request.getRequestURI())
            );
        } catch (ApiException e) {
            return ResponseEntity.status(503).body(
                    ErrorResponse.of(503, "API Service Error", e.getMessage(), request.getRequestURI())
            );
        } catch (Exception e) {
            log.warn("Failed to fetch skill code. characterName={}", characterName, e);
            return ResponseEntity.status(500).body(
                    ErrorResponse.of(500, "Internal Server Error", "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", request.getRequestURI())
            );
        }
    }
}

