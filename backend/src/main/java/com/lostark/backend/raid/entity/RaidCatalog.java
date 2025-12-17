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

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

