package com.lostark.backend.repository;

import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.CharacterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CharacterSnapshotRepository extends JpaRepository<CharacterSnapshot, Long> {
    Optional<CharacterSnapshot> findByCharacterAndResetDate(Character character, LocalDate resetDate);
}
