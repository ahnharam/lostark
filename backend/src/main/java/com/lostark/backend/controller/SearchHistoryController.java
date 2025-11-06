package com.lostark.backend.controller;

import com.lostark.backend.entity.SearchHistory;
import com.lostark.backend.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class SearchHistoryController {
    
    private final SearchHistoryService searchHistoryService;
    
    @GetMapping
    public ResponseEntity<List<SearchHistory>> getHistory(@RequestParam String userId) {
        List<SearchHistory> history = searchHistoryService.getSearchHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clearHistory(@RequestParam String userId) {
        searchHistoryService.clearSearchHistory(userId);
        return ResponseEntity.ok().build();
    }
}
