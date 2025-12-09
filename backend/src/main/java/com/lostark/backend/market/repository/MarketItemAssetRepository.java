package com.lostark.backend.market.repository;

import com.lostark.backend.market.entity.MarketItemAsset;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketItemAssetRepository extends JpaRepository<MarketItemAsset, Long> {
    Optional<MarketItemAsset> findByApiItemId(Long apiItemId);
}
