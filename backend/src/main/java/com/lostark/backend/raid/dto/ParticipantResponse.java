package com.lostark.backend.raid.dto;

import com.lostark.backend.raid.entity.ParticipantStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipantResponse {

    private Long id;

    private Long userId;

    private String discordUsername;

    private String characterName;

    private String characterClass;

    private String itemLevel;

    private ParticipantStatus status;

    private String changeRequestReason;

    private LocalDateTime respondedAt;
}
