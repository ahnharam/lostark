package com.lostark.backend.service;

import com.lostark.backend.entity.SearchHistory;
import com.lostark.backend.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    
    private final SearchHistoryRepository searchHistoryRepository;
    
    @Transactional
    public void addSearchHistory(String userId, String characterName) {
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setCharacterName(characterName);
        searchHistoryRepository.save(history);
    }
    
    public List<SearchHistory> getSearchHistory(String userId) {
        return searchHistoryRepository.findTop10ByUserIdOrderBySearchedAtDesc(userId);
    }
    
    @Transactional
    public void clearSearchHistory(String userId) {
        List<SearchHistory> histories = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
        searchHistoryRepository.deleteAll(histories);
    }
}
