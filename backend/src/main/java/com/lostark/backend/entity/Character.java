package com.lostark.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String characterName;
    
    private String serverName;
    
    private String characterClassName;
    
    private String itemAvgLevel;
    
    private String itemMaxLevel;
    
    @Column(columnDefinition = "TEXT")
    private String characterImage;
    
    private String expeditionLevel;
    
    private String pvpGradeName;
    
    private String guildName;
    
    @Column(columnDefinition = "TEXT")
    private String apiResponse;
    
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
}
