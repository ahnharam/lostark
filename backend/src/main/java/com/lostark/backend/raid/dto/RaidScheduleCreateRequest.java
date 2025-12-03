package com.lostark.backend.raid.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RaidScheduleCreateRequest {

    private String raidName;

    private String difficulty;

    private LocalDateTime scheduledAt;

    private String description;

    private Integer maxParticipants;

    private List<Long> invitedUserIds;  // 초대할 유저 ID 목록
}
