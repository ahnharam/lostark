package com.lostark.backend.controller;

import com.lostark.backend.dto.SiblingCharacterDto;
import com.lostark.backend.service.SiblingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siblings")
@RequiredArgsConstructor
public class SiblingController {
    
    private final SiblingService siblingService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<List<SiblingCharacterDto>> getSiblings(@PathVariable String characterName) {
        try {
            List<SiblingCharacterDto> siblings = siblingService.getSiblingCharacters(characterName);
            return ResponseEntity.ok(siblings);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
