package com.lostark.backend.raid.repository;

import com.lostark.backend.raid.entity.RaidSchedule;
import com.lostark.backend.raid.entity.RaidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RaidScheduleRepository extends JpaRepository<RaidSchedule, Long> {

    List<RaidSchedule> findByCreatorId(Long creatorId);

    List<RaidSchedule> findByStatus(RaidStatus status);

    @Query("SELECT r FROM RaidSchedule r WHERE r.scheduledAt >= :from AND r.scheduledAt <= :to")
    List<RaidSchedule> findByScheduledAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT r FROM RaidSchedule r WHERE r.status = :status AND r.scheduledAt >= :now ORDER BY r.scheduledAt ASC")
    List<RaidSchedule> findUpcomingByStatus(@Param("status") RaidStatus status, @Param("now") LocalDateTime now);
}
