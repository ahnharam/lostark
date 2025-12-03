package com.lostark.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gems")
@RequiredArgsConstructor
public class GemController {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    @GetMapping("/{characterName}")
    public ResponseEntity<JsonNode> getCharacterGems(@PathVariable String characterName) {
        JsonNode payload = lostArkProfileDomainService.fetchArmoryGemsRaw(characterName);
        if (payload == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payload);
    }
}
