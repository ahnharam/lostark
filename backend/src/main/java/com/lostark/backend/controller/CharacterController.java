package com.lostark.backend.controller;

import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    
    private final CharacterService characterService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<CharacterProfileDto> getCharacter(
            @PathVariable String characterName,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            CharacterProfileDto profile = characterService.getCharacterProfile(characterName, userId, force);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
