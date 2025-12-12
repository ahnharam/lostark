package com.lostark.backend.controller;

import com.lostark.backend.dto.AvatarDto;
import com.lostark.backend.service.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping("/{characterName}")
    public ResponseEntity<List<AvatarDto>> getAvatars(
            @PathVariable String characterName,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            log.info("Fetching avatars. characterName={} force={}", characterName, force);
            List<AvatarDto> avatars = avatarService.getCharacterAvatars(characterName, force);
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
