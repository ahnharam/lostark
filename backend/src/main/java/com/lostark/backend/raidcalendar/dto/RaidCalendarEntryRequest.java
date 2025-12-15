package com.lostark.backend.raidcalendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RaidCalendarEntryRequest {
    private String raidId;
    private String difficultyKey;
    private Integer gate;
    private LocalDateTime scheduledAt;
    private String memo;
}

