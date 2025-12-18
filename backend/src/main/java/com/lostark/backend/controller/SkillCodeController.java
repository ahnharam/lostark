package com.lostark.backend.controller;

import com.lostark.backend.dto.SkillCodeResponseDto;
import com.lostark.backend.service.SkillCodeService;
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
    public ResponseEntity<SkillCodeResponseDto> getSkillCode(@PathVariable String characterName) {
        try {
            String skillCode = skillCodeService.getSkillCode(characterName);
            SkillCodeResponseDto response = new SkillCodeResponseDto();
            response.setCharacterName(characterName);
            response.setSkillCode(skillCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Failed to fetch skill code. characterName={}", characterName, e);
            return ResponseEntity.notFound().build();
        }
    }
}

