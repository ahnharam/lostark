package com.lostark.backend.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "market_item_daily_stats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_market_item_daily_stat_item_date", columnNames = {"apiItemId", "statDate"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class MarketItemDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long apiItemId;

    @Column(nullable = false)
    private LocalDate statDate;

    private Double minPrice;

    private Double avgPrice;

    private Long tradeCount;

    private Long tradeVolume;

    private LocalDateTime fetchedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

