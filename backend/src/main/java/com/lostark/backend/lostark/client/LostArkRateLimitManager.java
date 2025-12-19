package com.lostark.backend.lostark.client;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class LostArkRateLimitManager {

    private static final long DEFAULT_RATE_LIMIT_BACKOFF_MS = 1000L;
    private static final long DEFAULT_MAINTENANCE_BACKOFF_MS = 60_000L;

    private final AtomicInteger limit = new AtomicInteger(0);
    private final AtomicInteger remaining = new AtomicInteger(0);
    private final AtomicLong resetEpochSeconds = new AtomicLong(0);
    private final AtomicLong rateLimitUntilMs = new AtomicLong(0);
    private final AtomicLong maintenanceUntilMs = new AtomicLong(0);

    public long getDelayMillis() {
        long now = System.currentTimeMillis();
        long maintenanceWait = Math.max(0L, maintenanceUntilMs.get() - now);
        long rateLimitWait = Math.max(0L, rateLimitUntilMs.get() - now);
        return Math.max(maintenanceWait, rateLimitWait);
    }

    public void updateFromHeaders(HttpHeaders headers, int statusCode) {
        if (headers == null) return;
        Integer limitValue = parseIntHeader(headers, "X-RateLimit-Limit");
        Integer remainingValue = parseIntHeader(headers, "X-RateLimit-Remaining");
        Long resetValue = parseLongHeader(headers, "X-RateLimit-Reset");

        if (limitValue != null) {
            limit.set(limitValue);
        }
        if (remainingValue != null) {
            remaining.set(remainingValue);
        }
        if (resetValue != null) {
            resetEpochSeconds.set(resetValue);
        }

        if (statusCode == 503) {
            registerMaintenance(headers);
            return;
        }

        if (statusCode == 429) {
            registerRateLimit(headers, resetValue);
            return;
        }

        if (remainingValue != null && remainingValue <= 0) {
            registerRateLimit(headers, resetValue);
            return;
        }

        if (remainingValue != null && remainingValue > 0) {
            long now = System.currentTimeMillis();
            long current = rateLimitUntilMs.get();
            if (current <= now) {
                rateLimitUntilMs.set(0L);
            }
        }
    }

    private void registerMaintenance(HttpHeaders headers) {
        long now = System.currentTimeMillis();
        long delayMs = resolveRetryAfterMs(headers, DEFAULT_MAINTENANCE_BACKOFF_MS);
        updateMax(maintenanceUntilMs, now + delayMs);
    }

    private void registerRateLimit(HttpHeaders headers, Long resetValue) {
        long now = System.currentTimeMillis();
        if (resetValue != null) {
            long resetMs = Instant.ofEpochSecond(resetValue).toEpochMilli();
            updateMax(rateLimitUntilMs, resetMs);
            return;
        }
        long delayMs = resolveRetryAfterMs(headers, DEFAULT_RATE_LIMIT_BACKOFF_MS);
        updateMax(rateLimitUntilMs, now + delayMs);
    }

    private long resolveRetryAfterMs(HttpHeaders headers, long fallbackMs) {
        String value = headers.getFirst("Retry-After");
        if (value != null && !value.isBlank()) {
            try {
                long seconds = Long.parseLong(value.trim());
                return Math.max(seconds * 1000L, fallbackMs);
            } catch (NumberFormatException ignored) {
                // fallback to default
            }
        }
        return fallbackMs;
    }

    private Integer parseIntHeader(HttpHeaders headers, String name) {
        String value = headers.getFirst(name);
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long parseLongHeader(HttpHeaders headers, String name) {
        String value = headers.getFirst(name);
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void updateMax(AtomicLong target, long value) {
        long prev;
        do {
            prev = target.get();
            if (value <= prev) return;
        } while (!target.compareAndSet(prev, value));
    }
}
