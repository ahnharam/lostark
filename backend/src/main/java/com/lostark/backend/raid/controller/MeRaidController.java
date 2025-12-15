package com.lostark.backend.raid.controller;

import com.lostark.backend.auth.AppUserPrincipal;
import com.lostark.backend.raid.dto.RaidMemberAddRequest;
import com.lostark.backend.raid.dto.RaidScheduleCreateRequest;
import com.lostark.backend.raid.dto.RaidScheduleResponse;
import com.lostark.backend.raid.service.MeRaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/raids")
@RequiredArgsConstructor
public class MeRaidController {

    private final MeRaidService meRaidService;

    // TODO(2차): 레이드 추천 조합/추천 멤버 API 추가
    // - 직업별 포지션(서폿/딜) 분류 + 시너지 규칙(시너지/버프 중복 등) 반영
    // - 레이드별 파티 사이즈/권장 서폿 수/최소 아이템레벨 등 정책(raid_catalog) 기반 추천

    @PostMapping
    public ResponseEntity<RaidScheduleResponse> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody RaidScheduleCreateRequest request
    ) {
        return ResponseEntity.ok(meRaidService.createRaid(principal.getAppUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<RaidScheduleResponse>> myRaids(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meRaidService.getMyRaids(principal.getAppUserId()));
    }

    @GetMapping("/{raidId}")
    public ResponseEntity<RaidScheduleResponse> get(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long raidId
    ) {
        return ResponseEntity.ok(meRaidService.getMyRaid(principal.getAppUserId(), raidId));
    }

    @PostMapping("/{raidId}/members")
    public ResponseEntity<RaidScheduleResponse> addMembers(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long raidId,
            @RequestBody RaidMemberAddRequest request
    ) {
        return ResponseEntity.ok(meRaidService.addMembers(principal.getAppUserId(), raidId, request.getUserIds()));
    }

    @DeleteMapping("/{raidId}/members/{participantId}")
    public ResponseEntity<RaidScheduleResponse> removeMember(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long raidId,
            @PathVariable Long participantId
    ) {
        return ResponseEntity.ok(meRaidService.removeMember(principal.getAppUserId(), raidId, participantId));
    }
}
