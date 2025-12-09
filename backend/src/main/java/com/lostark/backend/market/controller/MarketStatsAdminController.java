package com.lostark.backend.market.controller;

import com.lostark.backend.market.dto.MarketDailyStatDto;
import com.lostark.backend.market.repository.MarketDailyStatRepository;
import com.lostark.backend.market.repository.MarketItemAssetRepository;
import com.lostark.backend.market.scheduler.MarketStatsScheduler;
import com.lostark.backend.market.service.MarketSyncService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
public class MarketStatsAdminController {

    private final MarketDailyStatRepository marketDailyStatRepository;
    private final MarketItemAssetRepository marketItemAssetRepository;
    private final MarketSyncService marketSyncService;
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

        Page<com.lostark.backend.market.entity.MarketDailyStat> result;
        if (q != null && !q.isBlank()) {
            try {
                long apiItemId = Long.parseLong(q.trim());
                result = marketDailyStatRepository.findByApiItemIdOrderByStatDateDescFetchedAtDesc(apiItemId, pageable);
            } catch (NumberFormatException ignored) {
                result = marketDailyStatRepository.findByItemNameContainingIgnoreCaseOrderByStatDateDescFetchedAtDesc(q.trim(), pageable);
            }
        } else {
            result = marketDailyStatRepository.findAllByOrderByStatDateDescFetchedAtDesc(pageable);
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
        return ResponseEntity.accepted().body("capture started for " + (targetDate != null ? targetDate : "yesterday (KST)"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "running", marketStatsScheduler.isRunning()
        ));
    }
}
