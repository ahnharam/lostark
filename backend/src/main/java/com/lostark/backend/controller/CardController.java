package com.lostark.backend.controller;

import com.lostark.backend.dto.CardResponseDto;
import com.lostark.backend.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/{characterName}")
    public ResponseEntity<CardResponseDto> getCards(@PathVariable String characterName) {
        try {
            CardResponseDto response = cardService.getCards(characterName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
