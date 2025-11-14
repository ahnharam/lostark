package com.lostark.backend.controller;

import com.lostark.backend.dto.ArkGridResponseDto;
import com.lostark.backend.service.ArkGridService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ark-grid")
@RequiredArgsConstructor
public class ArkGridController {

    private final ArkGridService arkGridService;

    @GetMapping("/{characterName}")
    public ResponseEntity<ArkGridResponseDto> getArkGrid(@PathVariable String characterName) {
        try {
            ArkGridResponseDto response = arkGridService.getArkGrid(characterName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
