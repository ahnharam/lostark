package com.lostark.backend.market.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.market.MarketCategoryDto;
import com.lostark.backend.dto.market.MarketItemDto;
import com.lostark.backend.dto.market.MarketItemsRequest;
import com.lostark.backend.dto.market.MarketItemsResponse;
import com.lostark.backend.dto.market.MarketOptionsResponse;
import com.lostark.backend.dto.market.MarketSyncResultDto;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.lostark.client.LostArkApiClient;
import com.lostark.backend.market.entity.MarketCategory;
import com.lostark.backend.market.entity.MarketItem;
import com.lostark.backend.market.repository.MarketCategoryRepository;
import com.lostark.backend.market.repository.MarketItemRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSyncService {

    private final LostArkApiClient lostArkApiClient;
    private final MarketCategoryRepository marketCategoryRepository;
    private final MarketItemRepository marketItemRepository;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRY = 5;
    private static final long BASE_BACKOFF_MS = 1500L;
    private static final long MAX_BACKOFF_MS = 8000L;
    private static final long CALL_DELAY_MS = 150L;

    @Transactional
    public List<MarketCategory> syncCategories() {
        MarketOptionsResponse options = lostArkApiClient.getMarketOptions()
                .blockOptional()
                .orElseThrow(() -> new ApiException("거래소 카테고리를 불러오지 못했습니다."));

        marketCategoryRepository.deleteAllInBatch();

        List<MarketCategory> flattened = new ArrayList<>();
        if (!CollectionUtils.isEmpty(options.getCategories())) {
            for (MarketCategoryDto categoryDto : options.getCategories()) {
                collectCategory(categoryDto, null, 0, flattened);
            }
        }

        return marketCategoryRepository.saveAll(flattened);
    }

    @Transactional
    public MarketSyncResultDto syncItems(boolean leafOnly, int maxPagesPerCategory, int pageSize, boolean clearExisting) {
        List<MarketCategory> categories = leafOnly
                ? marketCategoryRepository.findByHasSubsFalse()
                : marketCategoryRepository.findAll();

        if (clearExisting) {
            marketItemRepository.deleteAllInBatch();
        }

        long totalSaved = 0;
        int pagesFetched = 0;

        for (MarketCategory category : categories) {
            int pageNo = 1;
            while (pageNo <= maxPagesPerCategory) {
                MarketItemsRequest request = MarketItemsRequest.builder()
                        .sort("RECENT_PRICE")
                        .categoryCode(category.getCode())
                        .itemTier(null)
                        .itemGrade(null)
                        .itemName(null)
                        .pageNo(pageNo)
                        .pageSize(pageSize)
                        .sortCondition("ASC")
                        .build();

                MarketItemsResponse response;
                try {
                    response = fetchMarketItemsWithRetry(request, category.getCode(), pageNo);
                } catch (Exception e) {
                    log.error("거래소 아이템 조회 실패 - categoryCode={}, page={}", category.getCode(), pageNo, e);
                    break;
                }

                if (response == null || CollectionUtils.isEmpty(response.getItems())) {
                    break;
                }

                pagesFetched++;
                List<MarketItem> toSave = mapItems(response.getItems(), category.getCode(), response.getPageNo());
                marketItemRepository.saveAll(toSave);
                totalSaved += toSave.size();

                int totalCount = response.getTotalCount() != null ? response.getTotalCount() : 0;
                int lastPage = pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : pageNo;
                if (pageNo >= lastPage || pageNo >= maxPagesPerCategory) {
                    break;
                }
                pageNo++;
                sleep(CALL_DELAY_MS); // 연속 호출 완화
            }
        }

        return MarketSyncResultDto.builder()
                .categoriesProcessed(categories.size())
                .itemsSaved(totalSaved)
                .pagesFetched(pagesFetched)
                .build();
    }

    public List<MarketCategory> getCategories() {
        return marketCategoryRepository.findAll();
    }

    public Page<MarketItem> getItems(Integer categoryCode, int page, int size) {
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        if (categoryCode == null) {
            return marketItemRepository.findAll(pageable);
        }
        return marketItemRepository.findByCategoryCode(categoryCode, pageable);
    }

    private void collectCategory(MarketCategoryDto dto, Integer parentCode, int depth, List<MarketCategory> out) {
        MarketCategory category = new MarketCategory();
        category.setCode(dto.getCode());
        category.setCodeName(dto.getCodeName());
        category.setParentCode(parentCode);
        category.setDepth(depth);
        category.setHasSubs(!CollectionUtils.isEmpty(dto.getSubs()));
        out.add(category);

        if (!CollectionUtils.isEmpty(dto.getSubs())) {
            for (MarketCategoryDto sub : dto.getSubs()) {
                collectCategory(sub, dto.getCode(), depth + 1, out);
            }
        }
    }

    private MarketItemsResponse fetchMarketItemsWithRetry(MarketItemsRequest request, Integer categoryCode, int pageNo) {
        int attempt = 0;
        long backoff = BASE_BACKOFF_MS;
        while (attempt < MAX_RETRY) {
            try {
                return lostArkApiClient.getMarketItems(request)
                        .blockOptional()
                        .orElse(null);
            } catch (WebClientResponseException.TooManyRequests e) {
                long waitMs = resolveRetryAfterMs(e, backoff);
                log.warn("거래소 아이템 429 응답 - categoryCode={}, page={}, attempt={}/{} | {}ms 후 재시도",
                        categoryCode, pageNo, attempt + 1, MAX_RETRY, waitMs);
                sleep(waitMs);
                backoff = Math.min((long) (backoff * 1.5), MAX_BACKOFF_MS);
                attempt++;
            }
        }
        return null;
    }

    private long resolveRetryAfterMs(WebClientResponseException e, long fallbackMs) {
        String retryAfter = e.getHeaders().getFirst("Retry-After");
        if (retryAfter != null) {
            try {
                // Retry-After가 초 단위로 내려오므로 ms로 변환
                long seconds = Long.parseLong(retryAfter.trim());
                return Math.max(seconds * 1000L, fallbackMs);
            } catch (NumberFormatException ignored) {
                // 포맷이 이상하면 fallback 사용
            }
        }
        return fallbackMs;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private List<MarketItem> mapItems(List<MarketItemDto> items, Integer categoryCode, Integer pageNo) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<MarketItem> result = new ArrayList<>();
        for (MarketItemDto dto : items) {
            MarketItem entity = new MarketItem();
            entity.setApiItemId(dto.getId());
            entity.setCategoryCode(categoryCode);
            entity.setPageNo(pageNo);
            entity.setName(dto.getName());
            entity.setGrade(dto.getGrade());
            entity.setIcon(dto.getIcon());
            entity.setBundleCount(dto.getBundleCount());
            entity.setTradeRemainCount(dto.getTradeRemainCount());
            entity.setYDayAvgPrice(dto.getYDayAvgPrice());
            entity.setRecentPrice(dto.getRecentPrice());
            entity.setCurrentMinPrice(dto.getCurrentMinPrice());
            entity.setFetchedAt(LocalDateTime.now());
            try {
                entity.setRaw(objectMapper.writeValueAsString(dto));
            } catch (JsonProcessingException e) {
                log.warn("거래소 아이템 JSON 직렬화 실패: {}", dto.getName(), e);
                entity.setRaw("{}");
            }
            result.add(entity);
        }
        return result;
    }
}
