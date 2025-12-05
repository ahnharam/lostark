package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketItemRepository extends JpaRepository<MarketItem, Long> {
    Page<MarketItem> findByCategoryCode(Integer categoryCode, Pageable pageable);
    Optional<MarketItem> findTopByCategoryCodeOrderByFetchedAtDesc(Integer categoryCode);
    Optional<MarketItem> findTopByOrderByFetchedAtDesc();
    Optional<MarketItem> findTopByApiItemIdOrderByFetchedAtDesc(Long apiItemId);
}
