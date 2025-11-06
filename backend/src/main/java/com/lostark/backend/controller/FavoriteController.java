package com.lostark.backend.controller;

import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    @PostMapping
    public ResponseEntity<Void> addFavorite(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String characterName = request.get("characterName");
        favoriteService.addFavorite(userId, characterName);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(@RequestParam String userId, @RequestParam String characterName) {
        favoriteService.removeFavorite(userId, characterName);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<List<CharacterProfileDto>> getFavorites(@RequestParam String userId) {
        List<CharacterProfileDto> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @RequestParam String userId, 
            @RequestParam String characterName) {
        boolean isFavorite = favoriteService.isFavorite(userId, characterName);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }
}
