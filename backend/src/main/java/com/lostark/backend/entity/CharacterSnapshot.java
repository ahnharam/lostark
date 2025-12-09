package com.lostark.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "character_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_character_reset_date", columnNames = {"character_id", "reset_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CharacterSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    /**
     * 리셋 기준 일자(06:00 KST 이전은 전날로 계산).
     */
    @Column(name = "reset_date", nullable = false)
    private LocalDate resetDate;

    /**
     * 실제 캡처 시각(KST 기준).
     */
    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    private String characterName;

    private String serverName;

    private String characterClassName;

    private Integer characterLevel;

    private String itemAvgLevel;

    private String itemMaxLevel;

    @Column(columnDefinition = "TEXT")
    private String characterImage;

    private String title;

    private String expeditionLevel;

    private String pvpGradeName;

    private String guildName;

    private String honorPoint;

    private String combatPower;

    private Double collectionScore;

    @Column(columnDefinition = "TEXT")
    private String statsJson;

    @Column(columnDefinition = "TEXT")
    private String apiResponse;

    @PrePersist
    protected void onCreate() {
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
    }
}
