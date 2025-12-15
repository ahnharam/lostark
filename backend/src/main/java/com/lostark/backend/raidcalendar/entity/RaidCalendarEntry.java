package com.lostark.backend.raidcalendar.entity;

import com.lostark.backend.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "raid_calendar_entries",
        indexes = {
                @Index(name = "idx_raid_calendar_entries_user_scheduled_at", columnList = "user_id, scheduled_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RaidCalendarEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "raid_id", nullable = false, length = 64)
    private String raidId;

    @Column(name = "difficulty_key", nullable = false, length = 32)
    private String difficultyKey;

    @Column(nullable = false)
    private Integer gate;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 255)
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
}
