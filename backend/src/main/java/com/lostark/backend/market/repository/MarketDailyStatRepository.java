package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketDailyStat;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketDailyStatRepository extends JpaRepository<MarketDailyStat, Long> {
    Optional<MarketDailyStat> findByApiItemIdAndStatDate(Long apiItemId, LocalDate statDate);
    Page<MarketDailyStat> findAllByOrderByStatDateDescFetchedAtDesc(Pageable pageable);
    Page<MarketDailyStat> findByApiItemIdOrderByStatDateDescFetchedAtDesc(Long apiItemId, Pageable pageable);
    Page<MarketDailyStat> findByItemNameContainingIgnoreCaseOrderByStatDateDescFetchedAtDesc(String keyword, Pageable pageable);
}
