package com.lostark.backend.raid.repository;

import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.entity.RaidParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RaidParticipantRepository extends JpaRepository<RaidParticipant, Long> {

    List<RaidParticipant> findByRaidScheduleId(Long raidScheduleId);

    List<RaidParticipant> findByUserId(Long userId);

    Optional<RaidParticipant> findByRaidScheduleIdAndUserId(Long raidScheduleId, Long userId);

    List<RaidParticipant> findByRaidScheduleIdAndStatus(Long raidScheduleId, ParticipantStatus status);

    boolean existsByRaidScheduleIdAndUserId(Long raidScheduleId, Long userId);
}
