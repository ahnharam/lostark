package com.lostark.backend.raid.entity;

import com.lostark.backend.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "raid_participants")
@Getter
@Setter
@NoArgsConstructor
public class RaidParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raid_schedule_id")
    private RaidSchedule raidSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String characterName;  // 참여 캐릭터명

    private String characterClass;  // 직업

    private String itemLevel;  // 아이템 레벨

    @Enumerated(EnumType.STRING)
    private ParticipantStatus status = ParticipantStatus.PENDING;

    private String changeRequestReason;  // 변경 요청 시 사유

    private String dmMessageId;  // 디스코드 DM 메시지 ID

    private LocalDateTime respondedAt;  // 응답 시간

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
