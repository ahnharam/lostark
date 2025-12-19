package com.lostark.backend.market.controller;

import com.lostark.backend.dto.market.MarketItemDetailDto;
import com.lostark.backend.dto.market.MarketItemDto;
import com.lostark.backend.dto.market.MarketItemRefreshRequest;
import com.lostark.backend.dto.market.MarketOptionsResponse;
import com.lostark.backend.dto.market.MarketSearchResponse;
import com.lostark.backend.market.entity.MarketCategory;
import com.lostark.backend.market.service.MarketSyncService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketController {

    private final MarketSyncService marketSyncService;

    @GetMapping("/options")
    public ResponseEntity<MarketOptionsResponse> getOptions() {
        return ResponseEntity.ok(marketSyncService.getOptions());
    }
    
    @PostMapping("/options/sync")
    public ResponseEntity<List<MarketCategory>> syncCategories() {
        return ResponseEntity.ok(marketSyncService.syncCategories());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MarketCategory>> getCategories() {
        return ResponseEntity.ok(marketSyncService.getCategories());
    }

    @GetMapping("/items")
    public ResponseEntity<MarketSearchResponse> searchItems(
            @RequestParam(required = false) Integer categoryCode,
            @RequestParam(required = false) List<Integer> categoryCodes,
            @RequestParam(required = false) String characterClass,
            @RequestParam(required = false) Integer itemTier,
            @RequestParam(required = false) String itemGrade,
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "RECENT_PRICE") String sort,
            @RequestParam(defaultValue = "ASC") String sortCondition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "3") int prefetchRange
    ) {
        MarketSearchResponse response = marketSyncService.searchMarketItemsByCategories(
                categoryCode,
                categoryCodes,
                characterClass,
                itemTier,
                itemGrade,
                itemName,
                sort,
                sortCondition,
                page,
                size,
                prefetchRange
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{apiItemId}/refresh")
    public ResponseEntity<MarketItemDto> refreshItem(
            @PathVariable Long apiItemId,
            @RequestBody(required = false) MarketItemRefreshRequest request
    ) {
        return ResponseEntity.ok(marketSyncService.refreshSingleItem(apiItemId, request));
    }

    @GetMapping("/items/{apiItemId}/detail")
    public ResponseEntity<MarketItemDetailDto> getItemDetail(@PathVariable Long apiItemId) {
        MarketItemDetailDto detail = marketSyncService.getItemDetail(apiItemId);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }
}
