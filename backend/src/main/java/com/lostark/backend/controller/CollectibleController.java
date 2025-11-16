package com.lostark.backend.controller;

import com.lostark.backend.dto.CollectibleDto;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.service.CollectibleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collectibles")
@RequiredArgsConstructor
public class CollectibleController {

    private final CollectibleService collectibleService;

    @GetMapping("/{characterName}")
    public ResponseEntity<List<CollectibleDto>> getCollectibles(@PathVariable String characterName) {
        try {
            return ResponseEntity.ok(collectibleService.getCollectibles(characterName));
        } catch (CharacterNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
