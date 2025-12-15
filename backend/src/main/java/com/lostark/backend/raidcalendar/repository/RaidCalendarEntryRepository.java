package com.lostark.backend.raidcalendar.repository;

import com.lostark.backend.raidcalendar.entity.RaidCalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaidCalendarEntryRepository extends JpaRepository<RaidCalendarEntry, Long> {
    List<RaidCalendarEntry> findByUserIdOrderByScheduledAtAsc(Long userId);
    void deleteByUserId(Long userId);
}

