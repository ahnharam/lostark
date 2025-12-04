package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketCategoryRepository extends JpaRepository<MarketCategory, Long> {
    List<MarketCategory> findByHasSubsFalse();
    Optional<MarketCategory> findByCode(Integer code);
}
