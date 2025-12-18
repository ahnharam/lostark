package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketItemDailyStat;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketItemDailyStatRepository extends JpaRepository<MarketItemDailyStat, Long> {

    Optional<MarketItemDailyStat> findByApiItemIdAndStatDate(Long apiItemId, LocalDate statDate);

    Page<MarketItemDailyStat> findAllByOrderByStatDateDescFetchedAtDesc(Pageable pageable);

    Page<MarketItemDailyStat> findByApiItemIdOrderByStatDateDescFetchedAtDesc(Long apiItemId, Pageable pageable);

    @Query("""
            select s
            from MarketItemDailyStat s
            where s.apiItemId in (
                select a.apiItemId
                from MarketItemAsset a
                where lower(a.name) like lower(concat('%', :keyword, '%'))
            )
            """)
    Page<MarketItemDailyStat> findByAssetNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}

