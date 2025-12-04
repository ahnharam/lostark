package com.lostark.backend.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "market_items")
@Getter
@Setter
@NoArgsConstructor
public class MarketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long apiItemId;

    private Integer categoryCode;

    private Integer pageNo;

    private String name;

    private String grade;

    private String icon;

    private Integer bundleCount;

    private Integer tradeRemainCount;

    private Double yDayAvgPrice;

    private Double recentPrice;

    private Double currentMinPrice;

    @Column(columnDefinition = "TEXT")
    private String raw;

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
