package com.lostark.backend.raidcalendar.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RaidCalendarEntryResponse {
    private Long id;
    private String raidId;
    private String difficultyKey;
    private Integer gate;
    private LocalDateTime scheduledAt;
    private String memo;
}

