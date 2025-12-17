package com.lostark.backend.raid.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public final class RaidScheduleKeyUtils {

    private static final DayOfWeek WEEKLY_RESET_DAY = DayOfWeek.WEDNESDAY;
    private static final LocalTime WEEKLY_RESET_TIME = LocalTime.of(6, 0);

    private RaidScheduleKeyUtils() {}

    /**
     * 수요일 06:00 KST(로스트아크 주간 리셋) 기준으로 weekKey를 계산합니다.
     * <p>
     * LocalDateTime에는 타임존 정보가 없으므로, 서버/클라이언트가 "KST 기준" 값으로 주고받는다는 전제를 둡니다.
     */
    public static LocalDate calculateWeekKey(LocalDateTime scheduledAt) {
        LocalDateTime candidateReset = scheduledAt
                .toLocalDate()
                .with(TemporalAdjusters.previousOrSame(WEEKLY_RESET_DAY))
                .atTime(WEEKLY_RESET_TIME);

        if (scheduledAt.isBefore(candidateReset)) {
            return candidateReset.minusDays(7).toLocalDate();
        }
        return candidateReset.toLocalDate();
    }
}
