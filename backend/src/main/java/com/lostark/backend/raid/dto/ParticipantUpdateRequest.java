package com.lostark.backend.raid.dto;

import com.lostark.backend.raid.entity.ParticipantStatus;
import lombok.Data;

@Data
public class ParticipantUpdateRequest {

    private ParticipantStatus status;

    private String characterName;

    private String changeRequestReason;
}
