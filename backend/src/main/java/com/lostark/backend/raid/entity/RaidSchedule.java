package com.lostark.backend.raid.entity;

import com.lostark.backend.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raid_schedules")
@Getter
@Setter
@NoArgsConstructor
public class RaidSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String raidName;  // 발리탄, 에기르, 베히모스 등

    private String difficulty;  // 노말, 하드

    @Column(name = "raid_key")
    private String raidKey;  // 정규화된 레이드 키 (예: t4Raids.ts의 id)

    private LocalDateTime scheduledAt;  // 레이드 예정 시간

    @Column(name = "week_key")
    private LocalDate weekKey;  // 주간 리셋 기준 키 (수요일 06:00 KST 기준)

    private String description;  // 추가 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private AppUser creator;  // 생성자

    @Enumerated(EnumType.STRING)
    private RaidStatus status = RaidStatus.RECRUITING;

    private Integer maxParticipants = 8;  // 최대 인원 (4인/8인)

    @OneToMany(mappedBy = "raidSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RaidParticipant> participants = new ArrayList<>();

    private String discordMessageId;  // 디스코드 공지 메시지 ID

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int getConfirmedCount() {
        return (int) participants.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.ACCEPTED)
                .count();
    }

    public boolean isFull() {
        return getConfirmedCount() >= maxParticipants;
    }
}
