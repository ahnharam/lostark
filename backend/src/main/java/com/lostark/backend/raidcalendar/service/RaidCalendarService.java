package com.lostark.backend.raidcalendar.service;

import com.lostark.backend.raidcalendar.dto.RaidCalendarEntryRequest;
import com.lostark.backend.raidcalendar.dto.RaidCalendarEntryResponse;
import com.lostark.backend.raidcalendar.entity.RaidCalendarEntry;
import com.lostark.backend.raidcalendar.repository.RaidCalendarEntryRepository;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RaidCalendarService {

    private final RaidCalendarEntryRepository raidCalendarEntryRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<RaidCalendarEntryResponse> getMyEntries(Long userId) {
        return raidCalendarEntryRepository.findByUserIdOrderByScheduledAtAsc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RaidCalendarEntryResponse> replaceMyEntries(Long userId, List<RaidCalendarEntryRequest> requests) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        raidCalendarEntryRepository.deleteByUserId(userId);

        List<RaidCalendarEntry> entries = requests.stream()
                .map(request -> toEntity(user, request))
                .collect(Collectors.toList());

        return raidCalendarEntryRepository.saveAll(entries)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RaidCalendarEntry toEntity(AppUser user, RaidCalendarEntryRequest request) {
        RaidCalendarEntry entry = new RaidCalendarEntry();
        entry.setUser(user);
        entry.setRaidId(request.getRaidId());
        entry.setDifficultyKey(request.getDifficultyKey());
        entry.setGate(request.getGate() != null ? request.getGate() : 0);
        entry.setScheduledAt(request.getScheduledAt());
        entry.setMemo(request.getMemo());
        return entry;
    }

    private RaidCalendarEntryResponse toResponse(RaidCalendarEntry entry) {
        return RaidCalendarEntryResponse.builder()
                .id(entry.getId())
                .raidId(entry.getRaidId())
                .difficultyKey(entry.getDifficultyKey())
                .gate(entry.getGate())
                .scheduledAt(entry.getScheduledAt())
                .memo(entry.getMemo())
                .build();
    }
}

