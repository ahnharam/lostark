package com.lostark.backend.market.controller;

import com.lostark.backend.market.dto.MarketDailyStatDto;
import com.lostark.backend.market.repository.MarketItemAssetRepository;
import com.lostark.backend.market.repository.MarketItemDailyStatRepository;
import com.lostark.backend.market.scheduler.MarketStatsScheduler;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/market-stats")
@RequiredArgsConstructor
@Slf4j
public class MarketStatsAdminController {

    private final MarketItemDailyStatRepository marketItemDailyStatRepository;
    private final MarketItemAssetRepository marketItemAssetRepository;
    private final MarketStatsScheduler marketStatsScheduler;

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String q
    ) {
        int pageNo = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 200));
        PageRequest pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "statDate", "fetchedAt"));

        Page<com.lostark.backend.market.entity.MarketItemDailyStat> result;
        if (q != null && !q.isBlank()) {
            try {
                long apiItemId = Long.parseLong(q.trim());
                result = marketItemDailyStatRepository.findByApiItemIdOrderByStatDateDescFetchedAtDesc(apiItemId, pageable);
            } catch (NumberFormatException ignored) {
                result = marketItemDailyStatRepository.findByAssetNameContainingIgnoreCase(q.trim(), pageable);
            }
        } else {
            result = marketItemDailyStatRepository.findAllByOrderByStatDateDescFetchedAtDesc(pageable);
        }

        List<MarketDailyStatDto> content = result.getContent().stream()
                .map(stat -> MarketDailyStatDto.of(
                        stat,
                        marketItemAssetRepository.findByApiItemId(stat.getApiItemId()).orElse(null)
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber(),
                "size", result.getSize()
        ));
    }

    @PostMapping("/capture")
    public ResponseEntity<String> triggerCapture(@RequestParam(required = false) String date) {
        LocalDate targetDate = null;
        if (date != null && !date.isBlank()) {
            try {
                targetDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd");
            }
        }
        boolean started = marketStatsScheduler.requestCapture(targetDate);
        if (!started) {
            return ResponseEntity.status(409).body("capture already running");
        }
        // INFO로 남겨서 "요청이 실제로 들어왔는지" 확인하기 쉽게 함
        log.info("[MarketStatsAdmin] manual capture accepted targetDate={}", targetDate);
        return ResponseEntity.accepted().body("capture started for " + (targetDate != null ? targetDate : "yesterday (KST)"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        LocalDate targetDate = marketStatsScheduler.getCurrentTargetDate();
        Instant startedAt = marketStatsScheduler.getStartedAt();
        Map<String, Object> body = new HashMap<>();
        body.put("running", marketStatsScheduler.isRunning());
        body.put("scanned", marketStatsScheduler.getScanned());
        body.put("saved", marketStatsScheduler.getSaved());
        body.put("targetDate", targetDate != null ? targetDate.toString() : "");
        body.put("startedAt", startedAt != null ? startedAt.toString() : "");
        return ResponseEntity.ok(body);
    }
}
