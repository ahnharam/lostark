package com.lostark.backend.market.controller;

import com.lostark.backend.dto.market.MarketSyncResultDto;
import com.lostark.backend.market.entity.MarketCategory;
import com.lostark.backend.market.entity.MarketItem;
import com.lostark.backend.market.service.MarketSyncService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketController {

    private final MarketSyncService marketSyncService;

    @PostMapping("/options/sync")
    public ResponseEntity<List<MarketCategory>> syncCategories() {
        return ResponseEntity.ok(marketSyncService.syncCategories());
    }

    @PostMapping("/items/sync")
    public ResponseEntity<MarketSyncResultDto> syncItems(
            @RequestParam(defaultValue = "true") boolean leafOnly,
            @RequestParam(defaultValue = "5") int maxPagesPerCategory,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean clearExisting
    ) {
        MarketSyncResultDto result = marketSyncService.syncItems(leafOnly, maxPagesPerCategory, pageSize, clearExisting);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MarketCategory>> getCategories() {
        return ResponseEntity.ok(marketSyncService.getCategories());
    }

    @GetMapping("/items")
    public ResponseEntity<Page<MarketItem>> getItems(
            @RequestParam(required = false) Integer categoryCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(marketSyncService.getItems(categoryCode, page, size));
    }
}
