package com.lostark.backend.raid.service;

import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.entity.RaidSchedule;
import com.lostark.backend.raid.repository.RaidParticipantRepository;
import com.lostark.backend.raid.util.RaidScheduleKeyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RaidWeeklyLockoutService {

    private final RaidParticipantRepository raidParticipantRepository;
    private final RaidCatalogService raidCatalogService;

    public void ensureScheduleKeys(RaidSchedule schedule) {
        if (schedule == null) return;

        if (schedule.getRaidKey() == null || schedule.getRaidKey().isBlank()) {
            schedule.setRaidKey(raidCatalogService.resolveRaidKey(null, schedule.getRaidName()));
        }
        if (schedule.getWeekKey() == null && schedule.getScheduledAt() != null) {
            schedule.setWeekKey(RaidScheduleKeyUtils.calculateWeekKey(schedule.getScheduledAt()));
        }
    }

    /**
     * 주간 참여 제한 정책 (수요일 06:00 KST 리셋 기준).
     * <p>
     * 현재는 "수락(ACCEPTED)되는 순간"부터 같은 weekKey/raidKey에 대해 캐릭터 중복 수락을 막습니다.
     * TODO: 정책이 "배정되는 순간(멤버 추가/캐릭터 지정 시점)"으로 바뀌면, 이 검증 호출 위치를 그 시점으로 이동하세요.
     */
    public void validateCanAccept(RaidSchedule schedule, String characterName) {
        if (characterName == null || characterName.isBlank()) return;
        ensureScheduleKeys(schedule);

        if (schedule == null || schedule.getRaidKey() == null || schedule.getWeekKey() == null) return;

        long acceptedCount = raidParticipantRepository.countByScheduleWeekKeyAndScheduleRaidKeyAndStatusAndCharacterName(
                schedule.getWeekKey(),
                schedule.getRaidKey(),
                ParticipantStatus.ACCEPTED,
                characterName
        );

        if (acceptedCount > 0) {
            throw new IllegalArgumentException("해당 캐릭터는 이번 주(수요일 06:00 KST 리셋 기준)에 이미 같은 레이드를 수락했습니다.");
        }
    }
}
