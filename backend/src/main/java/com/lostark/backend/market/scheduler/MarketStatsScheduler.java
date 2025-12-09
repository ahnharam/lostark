package com.lostark.backend.market.scheduler;

import com.lostark.backend.market.service.MarketSyncService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketStatsScheduler {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MarketSyncService marketSyncService;

    @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
    public void capturePreviousDay() {
        LocalDate targetDate = LocalDate.now(ZONE_SEOUL).minusDays(1);
        runCapture(targetDate, "daily-0430");
    }

    @Scheduled(cron = "0 5 6 * * WED", zone = "Asia/Seoul")
    public void capturePostResetWednesday() {
        LocalDate targetDate = LocalDate.now(ZONE_SEOUL);
        runCapture(targetDate, "wednesday-0605");
    }

    public boolean requestCapture(LocalDate targetDate) {
        LocalDate target = targetDate != null ? targetDate : LocalDate.now(ZONE_SEOUL).minusDays(1);
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        // 비동기로 실행해 관리 페이지 호출을 블로킹하지 않음
        new Thread(() -> runCapture(target, "manual"), "market-stats-manual").start();
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    private void runCapture(LocalDate targetDate, String tag) {
        if (!running.compareAndSet(false, true)) {
            log.warn("[MarketStatsScheduler] skip {} because another job is running", tag);
            return;
        }
        try {
            log.info("[MarketStatsScheduler] start {} targetDate={}", tag, targetDate);
            marketSyncService.captureDailyStats(targetDate);
            log.info("[MarketStatsScheduler] finished {}", tag);
        } catch (Exception e) {
            log.error("[MarketStatsScheduler] job {} failed", tag, e);
        } finally {
            running.set(false);
        }
    }
}
