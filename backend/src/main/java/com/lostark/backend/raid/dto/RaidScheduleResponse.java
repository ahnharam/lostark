package com.lostark.backend.raid.dto;

import com.lostark.backend.raid.entity.RaidStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RaidScheduleResponse {

    private Long id;

    private String raidName;

    private String difficulty;

    private LocalDateTime scheduledAt;

    private String description;

    private RaidStatus status;

    private Integer maxParticipants;

    private Integer confirmedCount;

    private String creatorName;

    private List<ParticipantResponse> participants;

    private LocalDateTime createdAt;
}
