package com.lostark.backend.controller;

import com.lostark.backend.dto.SkillMenuResponseDto;
import com.lostark.backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping("/{characterName}")
    public ResponseEntity<SkillMenuResponseDto> getCharacterSkills(@PathVariable String characterName) {
        SkillMenuResponseDto response = skillService.getSkillMenu(characterName);
        return ResponseEntity.ok(response);
    }
}
