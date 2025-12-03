package com.lostark.backend.raid.controller;

import com.lostark.backend.raid.dto.ParticipantUpdateRequest;
import com.lostark.backend.raid.dto.RaidScheduleCreateRequest;
import com.lostark.backend.raid.dto.RaidScheduleResponse;
import com.lostark.backend.raid.service.RaidParticipantService;
import com.lostark.backend.raid.service.RaidScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/raids")
@RequiredArgsConstructor
public class RaidScheduleController {

    private final RaidScheduleService raidScheduleService;
    private final RaidParticipantService raidParticipantService;

    /**
     * 레이드 일정 생성
     */
    @PostMapping
    public ResponseEntity<RaidScheduleResponse> createRaid(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RaidScheduleCreateRequest request) {
        RaidScheduleResponse response = raidScheduleService.createRaidSchedule(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 레이드 일정 상세 조회
     */
    @GetMapping("/{raidId}")
    public ResponseEntity<RaidScheduleResponse> getRaid(@PathVariable Long raidId) {
        RaidScheduleResponse response = raidScheduleService.getRaidSchedule(raidId);
        return ResponseEntity.ok(response);
    }

    /**
     * 예정된 레이드 목록 조회
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<RaidScheduleResponse>> getUpcomingRaids() {
        List<RaidScheduleResponse> response = raidScheduleService.getUpcomingRaids();
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 생성한 레이드 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<RaidScheduleResponse>> getMyRaids(
            @RequestHeader("X-User-Id") Long userId) {
        List<RaidScheduleResponse> response = raidScheduleService.getMyRaids(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 레이드 취소
     */
    @DeleteMapping("/{raidId}")
    public ResponseEntity<Void> cancelRaid(
            @PathVariable Long raidId,
            @RequestHeader("X-User-Id") Long userId) {
        raidScheduleService.cancelRaid(raidId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 추가 유저 초대
     */
    @PostMapping("/{raidId}/invite")
    public ResponseEntity<Void> inviteUsers(
            @PathVariable Long raidId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, List<Long>> request) {
        List<Long> userIds = request.get("userIds");
        raidScheduleService.inviteUsers(raidId, userId, userIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 참가 상태 업데이트 (웹/앱용)
     */
    @PatchMapping("/participants/{participantId}")
    public ResponseEntity<Void> updateParticipantStatus(
            @PathVariable Long participantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ParticipantUpdateRequest request) {
        raidParticipantService.updateParticipantStatus(
                participantId, userId, request.getStatus(), request.getChangeRequestReason());
        return ResponseEntity.ok().build();
    }
}
