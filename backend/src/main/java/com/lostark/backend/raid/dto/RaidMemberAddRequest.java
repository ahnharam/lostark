package com.lostark.backend.raid.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RaidMemberAddRequest {
    private List<Long> userIds;
}

