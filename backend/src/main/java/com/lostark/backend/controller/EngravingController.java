package com.lostark.backend.controller;

import com.lostark.backend.dto.EngravingEffectDto;
import com.lostark.backend.service.EngravingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engravings")
@RequiredArgsConstructor
public class EngravingController {
    
    private final EngravingService engravingService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<List<EngravingEffectDto>> getEngravings(@PathVariable String characterName) {
        try {
            List<EngravingEffectDto> engravings = engravingService.getCharacterEngravings(characterName);
            return ResponseEntity.ok(engravings);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
