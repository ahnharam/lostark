package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketItemRepository extends JpaRepository<MarketItem, Long> {
    Page<MarketItem> findByCategoryCode(Integer categoryCode, Pageable pageable);
}
