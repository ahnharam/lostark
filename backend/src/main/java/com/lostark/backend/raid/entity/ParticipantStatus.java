package com.lostark.backend.raid.entity;

public enum ParticipantStatus {
    PENDING,          // 응답 대기
    ACCEPTED,         // 참여 확정
    DECLINED,         // 불참
    CHANGE_REQUESTED  // 시간 변경 요청
}
