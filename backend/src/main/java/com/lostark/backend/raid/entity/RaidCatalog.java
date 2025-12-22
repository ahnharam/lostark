package com.lostark.backend.raid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "raid_catalog")
@Getter
@Setter
@NoArgsConstructor
public class RaidCatalog {

    @Id
    @Column(name = "raid_key", length = 64)
    private String raidKey;

    @Column(name = "raid_name", nullable = false, unique = true, length = 128)
    private String raidName;

    @Column(name = "abbreviation", length = 64)
    private String abbreviation;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "item_level")
    private Integer itemLevel;

    @Column(name = "item_level_single")
    private Integer itemLevelSingle;

    @Column(name = "item_level_normal")
    private Integer itemLevelNormal;

    @Column(name = "item_level_hard")
    private Integer itemLevelHard;

    @Column(name = "item_level_nightmare")
    private Integer itemLevelNightmare;

    @Column(name = "gold_reward")
    private Integer goldReward;

    @Column(name = "gold_single")
    private Integer goldSingle;

    @Column(name = "gold_single_trade")
    private Integer goldSingleTrade;

    @Column(name = "gold_single_bound")
    private Integer goldSingleBound;

    @Column(name = "gold_normal")
    private Integer goldNormal;

    @Column(name = "gold_normal_trade")
    private Integer goldNormalTrade;

    @Column(name = "gold_normal_bound")
    private Integer goldNormalBound;

    @Column(name = "gold_hard")
    private Integer goldHard;

    @Column(name = "gold_hard_trade")
    private Integer goldHardTrade;

    @Column(name = "gold_hard_bound")
    private Integer goldHardBound;

    @Column(name = "gold_nightmare")
    private Integer goldNightmare;

    @Column(name = "gold_nightmare_trade")
    private Integer goldNightmareTrade;

    @Column(name = "gold_nightmare_bound")
    private Integer goldNightmareBound;

    @Column(name = "difficulty_codes", length = 128)
    private String difficultyCodes;

    @Column(name = "party_size")
    private Integer partySize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
