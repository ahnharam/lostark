package com.lostark.backend.market.migration;

import com.lostark.backend.market.entity.MarketDailyStat;
import com.lostark.backend.market.entity.MarketItemAsset;
import com.lostark.backend.market.entity.MarketItemDailyStat;
import com.lostark.backend.market.repository.MarketDailyStatRepository;
import com.lostark.backend.market.repository.MarketItemAssetRepository;
import com.lostark.backend.market.repository.MarketItemDailyStatRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Legacy 테이블(market_daily_stats)의 데이터를 신규 구조(아이템 메타 + 일별 지표)로 마이그레이션한다.
 *
 * <p>신규 일별 지표 테이블: market_item_daily_stats
 * <p>아이템 메타 테이블: market_item_assets (categoryCode 컬럼 추가)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDailyStatsMigration implements ApplicationRunner {

    private static final int PAGE_SIZE = 500;

    private final MarketDailyStatRepository legacyRepository;
    private final MarketItemDailyStatRepository dailyStatRepository;
    private final MarketItemAssetRepository assetRepository;

    @Override
    public void run(ApplicationArguments args) {
        long legacyCount = legacyRepository.count();
        if (legacyCount <= 0) {
            return;
        }

        long newCount = dailyStatRepository.count();
        if (newCount >= legacyCount) {
            return;
        }

        log.info("[MarketDailyStatsMigration] start legacyCount={} newCount={}", legacyCount, newCount);

        int page = 0;
        Set<Long> updatedAssetIds = new HashSet<>();
        while (true) {
            Page<MarketDailyStat> legacyPage = legacyRepository.findAll(PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id")));
            if (legacyPage.isEmpty()) {
                break;
            }
            legacyPage.getContent().forEach(stat -> migrateOne(stat, updatedAssetIds));
            if (!legacyPage.hasNext()) {
                break;
            }
            page++;
        }

        log.info("[MarketDailyStatsMigration] finished legacyCount={} newCount={}", legacyCount, dailyStatRepository.count());
    }

    private void migrateOne(MarketDailyStat legacyStat, Set<Long> updatedAssetIds) {
        if (legacyStat.getApiItemId() == null || legacyStat.getStatDate() == null) {
            return;
        }

        Long apiItemId = legacyStat.getApiItemId();

        MarketItemAsset asset = assetRepository.findByApiItemId(apiItemId).orElseGet(() -> {
            MarketItemAsset created = new MarketItemAsset();
            created.setApiItemId(apiItemId);
            created.setName(legacyStat.getItemName());
            created.setCategoryCode(legacyStat.getCategoryCode());
            return created;
        });

        if (!updatedAssetIds.contains(apiItemId)) {
            boolean dirty = false;
            if (asset.getCategoryCode() == null && legacyStat.getCategoryCode() != null) {
                asset.setCategoryCode(legacyStat.getCategoryCode());
                dirty = true;
            }
            if ((asset.getName() == null || asset.getName().isBlank()) && legacyStat.getItemName() != null) {
                asset.setName(legacyStat.getItemName());
                dirty = true;
            }
            if (dirty) {
                assetRepository.save(asset);
            } else if (asset.getId() == null) {
                assetRepository.save(asset);
            }
            updatedAssetIds.add(apiItemId);
        }

        MarketItemDailyStat stat = dailyStatRepository
                .findByApiItemIdAndStatDate(apiItemId, legacyStat.getStatDate())
                .orElseGet(MarketItemDailyStat::new);
        stat.setApiItemId(apiItemId);
        stat.setStatDate(legacyStat.getStatDate());
        stat.setMinPrice(legacyStat.getMinPrice());
        stat.setAvgPrice(legacyStat.getAvgPrice());
        stat.setTradeCount(legacyStat.getTradeCount());
        stat.setTradeVolume(legacyStat.getTradeVolume());
        stat.setFetchedAt(legacyStat.getFetchedAt());
        dailyStatRepository.save(stat);
    }
}

