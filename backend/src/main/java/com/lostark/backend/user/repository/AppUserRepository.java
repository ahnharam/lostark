package com.lostark.backend.user.repository;

import com.lostark.backend.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    Optional<AppUser> findByKakaoId(String kakaoId);
    
    Optional<AppUser> findByDiscordId(String discordId);
    
    Optional<AppUser> findByMainCharacterName(String mainCharacterName);
}
