package com.lostark.backend.controller;

import com.lostark.backend.dto.ProfileRankingResponseDto;
import com.lostark.backend.dto.RankingResponseDto;
import com.lostark.backend.service.ProfileRankingService;
import com.lostark.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final ProfileRankingService profileRankingService;

    @GetMapping
    public ResponseEntity<RankingResponseDto> getRankings(
            @RequestParam String leaderboardCode,
            @RequestParam(required = false) String seasonId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false) String characterName
    ) {
        RankingResponseDto response = rankingService.getRankings(leaderboardCode, seasonId, page, characterName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{characterName}")
    public ResponseEntity<ProfileRankingResponseDto> getProfileRanking(@PathVariable String characterName) {
        ProfileRankingResponseDto response = profileRankingService.getProfileRanking(characterName);
        return ResponseEntity.ok(response);
    }
}
