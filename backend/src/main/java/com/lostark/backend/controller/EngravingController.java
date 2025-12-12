package com.lostark.backend.controller;

import com.lostark.backend.dto.EngravingEffectDto;
import com.lostark.backend.service.EngravingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/engravings")
@RequiredArgsConstructor
public class EngravingController {
    
    private final EngravingService engravingService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<List<EngravingEffectDto>> getEngravings(
            @PathVariable String characterName,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            log.info("Fetching engravings. characterName={} force={}", characterName, force);
            List<EngravingEffectDto> engravings = engravingService.getCharacterEngravings(characterName, force);
            return ResponseEntity.ok(engravings);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
