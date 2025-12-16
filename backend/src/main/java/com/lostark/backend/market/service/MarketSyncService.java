package com.lostark.backend.market.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.market.MarketCategoryDto;
import com.lostark.backend.dto.market.MarketItemDetailDto;
import com.lostark.backend.dto.market.MarketItemDto;
import com.lostark.backend.dto.market.MarketItemStatDto;
import com.lostark.backend.dto.market.MarketItemRefreshRequest;
import com.lostark.backend.dto.market.MarketItemsRequest;
import com.lostark.backend.dto.market.MarketItemsResponse;
import com.lostark.backend.dto.market.MarketOptionsResponse;
import com.lostark.backend.dto.market.MarketSearchResponse;
import com.lostark.backend.dto.market.MarketSyncResultDto;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.lostark.client.LostArkApiClient;
import com.lostark.backend.market.entity.MarketCategory;
import com.lostark.backend.market.entity.MarketDailyStat;
import com.lostark.backend.market.entity.MarketOptionMeta;
import com.lostark.backend.market.repository.MarketCategoryRepository;
import com.lostark.backend.market.repository.MarketDailyStatRepository;
import com.lostark.backend.market.repository.MarketItemAssetRepository;
import com.lostark.backend.market.repository.MarketOptionMetaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MarketOptionMetaRepository marketOptionMetaRepository;
    private final MarketDailyStatRepository marketDailyStatRepository;
    private final MarketItemAssetRepository marketItemAssetRepository;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRY = 5;
    private static final long BASE_BACKOFF_MS = 1500L;
    private static final long MAX_BACKOFF_MS = 8000L;
    private static final long CALL_DELAY_MS = 150L;
    private static final long ITEM_REFRESH_COOLDOWN_MS = 5 * 1000L;
    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final Map<Integer, Long> categoryFetchTimestamps = new ConcurrentHashMap<>();
    private final Map<Long, Long> itemRefreshLocks = new ConcurrentHashMap<>();

    @Transactional
    public List<MarketCategory> syncCategories() {
        MarketOptionsResponse options = lostArkApiClient.getMarketOptions()
                .blockOptional()
                .orElseThrow(() -> new ApiException("거래소 카테고리를 불러오지 못했습니다."));

        marketCategoryRepository.deleteAllInBatch();
        marketOptionMetaRepository.deleteAllInBatch();

        List<MarketCategory> flattened = new ArrayList<>();
        if (!CollectionUtils.isEmpty(options.getCategories())) {
            for (MarketCategoryDto categoryDto : options.getCategories()) {
                collectCategory(categoryDto, null, 0, flattened);
            }
        }

        marketCategoryRepository.saveAll(flattened);
        saveOptionMeta(options);
        return marketCategoryRepository.findAll();
    }

    public MarketOptionsResponse getOptions() {
        // 우선 DB에 저장된 옵션을 반환하고, 없을 경우 API를 호출하여 저장한 뒤 반환
        return marketOptionMetaRepository.findAll().stream().findFirst()
                .map(this::toOptionsResponse)
                .orElseGet(() -> {
                    MarketOptionsResponse fresh = lostArkApiClient.getMarketOptions()
                            .blockOptional()
                            .orElseThrow(() -> new ApiException("거래소 옵션을 불러오지 못했습니다."));
                    saveOptionMeta(fresh);
                    return fresh;
                });
    }

    @Transactional
    public MarketSyncResultDto syncItems(boolean leafOnly, int maxPagesPerCategory, int pageSize, boolean clearExisting) {
        throw new ApiException("전체 리스트 일괄 수집은 비활성화되었습니다.");
    }

    public List<MarketCategory> getCategories() {
        return marketCategoryRepository.findAll();
    }

    /**
     * 카테고리/정렬/직업/페이지 조건에 맞춰 필요한 페이지만 불러오고,
     * 앞뒤로 prefetchRange만큼 추가 페이지를 같이 가져온다.
     */
    public MarketSearchResponse searchMarketItems(Integer categoryCode,
                                                  String characterClass,
                                                  Integer itemTier,
                                                  String itemGrade,
                                                  String itemName,
                                                  String sort,
                                                  String sortCondition,
                                                  int page,
                                                  int size,
                                                  int prefetchRange) {
        if (categoryCode == null) {
            throw new ApiException("categoryCode가 필요합니다.");
        }

        int pageNo = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int range = Math.max(prefetchRange, 0);

        MarketItemsRequest baseRequest = MarketItemsRequest.builder()
                .sort(sort != null ? sort : "RECENT_PRICE")
                .sortCondition(sortCondition != null ? sortCondition : "ASC")
                .categoryCode(categoryCode)
                .characterClass(characterClass)
                .itemTier(itemTier)
                .itemGrade(itemGrade)
                .itemName(itemName)
                .build();

        MarketItemsRequest effectiveRequest = baseRequest;
        PageBundle firstBundle = fetchPageBundle(effectiveRequest, categoryCode, pageNo, pageSize);
        if (firstBundle.items().isEmpty()) {
            return MarketSearchResponse.builder()
                    .categoryCode(categoryCode)
                    .characterClass(characterClass)
                    .sort(baseRequest.getSort())
                    .sortCondition(baseRequest.getSortCondition())
                    .itemTier(itemTier)
                    .itemGrade(itemGrade)
                    .itemName(itemName)
                    .page(pageNo)
                    .pageSize(pageSize)
                    .totalCount(firstBundle.totalCount())
                    .totalPages(0)
                    .pages(Collections.emptyMap())
                    .fetchedAt(System.currentTimeMillis())
                    .build();
        }

        int totalCount = firstBundle.totalCount() != null ? firstBundle.totalCount() : firstBundle.items().size();
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : pageNo;

        Map<Integer, List<MarketItemDto>> pages = new HashMap<>();
        pages.put(pageNo, firstBundle.items());

        int start = Math.max(1, pageNo - range);
        int end = Math.min(totalPages, pageNo + range);

        for (int p = start; p <= end; p++) {
            if (p == pageNo) continue;
            PageBundle bundle = fetchPageBundle(effectiveRequest, categoryCode, p, pageSize);
            if (!CollectionUtils.isEmpty(bundle.items())) {
                pages.put(p, bundle.items());
            }
        }

        categoryFetchTimestamps.put(categoryCode, System.currentTimeMillis());

        return MarketSearchResponse.builder()
                .categoryCode(categoryCode)
                .characterClass(characterClass)
                .sort(baseRequest.getSort())
                .sortCondition(baseRequest.getSortCondition())
                .itemTier(itemTier)
                .itemGrade(itemGrade)
                .itemName(itemName)
                .page(pageNo)
                    .pageSize(pageSize)
                    .totalCount(totalCount)
                    .totalPages(totalPages)
                    .pages(pages)
                .fetchedAt(System.currentTimeMillis())
                .build();
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

    private PageBundle fetchPageBundle(MarketItemsRequest baseRequest, Integer categoryCode, int uiPage, int uiPageSize) {
        int desiredSize = Math.max(uiPageSize, 1);
        int apiPageSize = Math.min(desiredSize, 10); // LostArk API 최대 사이즈 가정
        int startIndex = (uiPage - 1) * desiredSize;
        int startApiPage = (startIndex / apiPageSize) + 1;
        int skip = startIndex % apiPageSize;

        List<MarketItemDto> combined = new ArrayList<>();
        Integer totalCount = null;

        int currentApiPage = startApiPage;
        while (combined.size() < desiredSize) {
            MarketItemsRequest req = MarketItemsRequest.builder()
                    .sort(baseRequest.getSort())
                    .sortCondition(baseRequest.getSortCondition())
                    .categoryCode(baseRequest.getCategoryCode())
                    .characterClass(baseRequest.getCharacterClass())
                    .itemTier(baseRequest.getItemTier())
                    .itemGrade(baseRequest.getItemGrade())
                    .itemName(baseRequest.getItemName())
                    .pageNo(currentApiPage)
                    .pageSize(apiPageSize)
                    .build();

            MarketItemsResponse resp = fetchMarketItemsWithRetry(req, categoryCode, currentApiPage);
            if (resp == null || CollectionUtils.isEmpty(resp.getItems())) {
                break;
            }
            // debug: ensure API returns previous-day average price
            if (log.isDebugEnabled() && !CollectionUtils.isEmpty(resp.getItems())) {
                MarketItemDto sample = resp.getItems().get(0);
                log.debug("[MarketSync] page {} sample item {} yDayAvgPrice={}", currentApiPage, sample.getName(), sample.getYDayAvgPrice());
            }
            if (totalCount == null && resp.getTotalCount() != null) {
                totalCount = resp.getTotalCount();
            }

            List<MarketItemDto> items = resp.getItems();
            if (currentApiPage == startApiPage && skip > 0 && items.size() > skip) {
                items = items.subList(skip, items.size());
            }
            combined.addAll(items);
            if (items.size() < apiPageSize) {
                break;
            }
            currentApiPage++;
        }

        if (totalCount == null) {
            totalCount = combined.size();
        }
        return new PageBundle(combined, totalCount);
    }

    /**
     * 지정한 statDate(예: 전일) 기준 거래 데이터를 저장한다.
     */
    public void captureDailyStats(LocalDate statDate) {
        LocalDate targetDate = statDate != null ? statDate : LocalDate.now(ZONE_SEOUL).minusDays(1);
        List<MarketCategory> leafCategories = marketCategoryRepository.findAll().stream()
                .filter(cat -> !cat.isHasSubs())
                .toList();
        AtomicInteger saved = new AtomicInteger();
        AtomicInteger scanned = new AtomicInteger();

        for (MarketCategory category : leafCategories) {
            int page = 1;
            int pageSize = 10;
            while (true) {
                MarketItemsRequest request = MarketItemsRequest.builder()
                        .sort("RECENT_PRICE")
                        .sortCondition("ASC")
                        .categoryCode(category.getCode())
                        .pageNo(page)
                        .pageSize(pageSize)
                        .build();

                MarketItemsResponse resp = fetchMarketItemsWithRetry(request, category.getCode(), page);
                if (resp == null || CollectionUtils.isEmpty(resp.getItems())) {
                    break;
                }
                resp.getItems().forEach(item -> {
                    scanned.incrementAndGet();
                    if (persistDailyStat(item, category.getCode(), targetDate)) {
                        saved.incrementAndGet();
                    }
                });

                Integer totalCount = resp.getTotalCount();
                int totalPages = totalCount != null && pageSize > 0
                        ? (int) Math.ceil((double) totalCount / pageSize)
                        : page + 1;
                if (page >= totalPages || resp.getItems().size() < pageSize) {
                    break;
                }
                page++;
                sleep(CALL_DELAY_MS);
            }
        }
        log.info("[MarketStats] captureDailyStats date={} scanned={} saved={}", targetDate, scanned.get(), saved.get());
    }

    private boolean persistDailyStat(MarketItemDto itemDto, Integer categoryCode, LocalDate statDate) {
        if (itemDto == null || itemDto.getId() == null) {
            return false;
        }
        MarketItemDetailDto detail = getItemDetail(itemDto.getId());
        sleep(CALL_DELAY_MS);
        if (detail == null || CollectionUtils.isEmpty(detail.getStats())) {
            return false;
        }
        MarketItemStatDto targetStat = detail.getStats().stream()
                .filter(stat -> stat.getDate() != null && stat.getDate().isEqual(statDate))
                .findFirst()
                .orElse(null);
        if (targetStat == null) {
            return false;
        }

        Double avgPrice = targetStat.getAvgPrice();
        Long tradeCount = targetStat.getTradeCount() != null ? targetStat.getTradeCount().longValue() : null;
        long bundleCount = firstNonNull(
                detail.getBundleCount(),
                itemDto.getBundleCount(),
                1
        );
        Long tradeVolume = tradeCount != null ? tradeCount * bundleCount : null;
        Double minPrice = firstNonNull(itemDto.getCurrentMinPrice(), itemDto.getRecentPrice(), null);

        if (avgPrice == null && tradeCount == null && tradeVolume == null && minPrice == null) {
            return false;
        }

        MarketDailyStat stat = marketDailyStatRepository
                .findByApiItemIdAndStatDate(itemDto.getId(), statDate)
                .orElseGet(MarketDailyStat::new);
        stat.setApiItemId(itemDto.getId());
        stat.setCategoryCode(categoryCode);
        stat.setItemName(itemDto.getName());
        stat.setStatDate(statDate);
        stat.setMinPrice(minPrice);
        stat.setAvgPrice(avgPrice);
        stat.setTradeCount(tradeCount);
        stat.setTradeVolume(tradeVolume);
        stat.setFetchedAt(LocalDateTime.now(ZONE_SEOUL));

        marketDailyStatRepository.save(stat);

        upsertAsset(itemDto);
        return true;
    }

    private void upsertAsset(MarketItemDto itemDto) {
        marketItemAssetRepository.findByApiItemId(itemDto.getId())
                .ifPresentOrElse(asset -> {
                    boolean dirty = false;
                    if (itemDto.getName() != null && !itemDto.getName().equals(asset.getName())) {
                        asset.setName(itemDto.getName());
                        dirty = true;
                    }
                    if (itemDto.getIcon() != null && !itemDto.getIcon().equals(asset.getIcon())) {
                        asset.setIcon(itemDto.getIcon());
                        dirty = true;
                    }
                    if (itemDto.getGrade() != null && !itemDto.getGrade().equals(asset.getGrade())) {
                        asset.setGrade(itemDto.getGrade());
                        dirty = true;
                    }
                    if (dirty) {
                        marketItemAssetRepository.save(asset);
                    }
                }, () -> {
                    var asset = new com.lostark.backend.market.entity.MarketItemAsset();
                    asset.setApiItemId(itemDto.getId());
                    asset.setName(itemDto.getName());
                    asset.setIcon(itemDto.getIcon());
                    asset.setGrade(itemDto.getGrade());
                    marketItemAssetRepository.save(asset);
                });
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

    private void saveOptionMeta(MarketOptionsResponse options) {
        MarketOptionMeta meta = new MarketOptionMeta();
        try {
            meta.setItemGradesJson(objectMapper.writeValueAsString(options.getItemGrades()));
            meta.setItemTiersJson(objectMapper.writeValueAsString(options.getItemTiers()));
            meta.setClassesJson(objectMapper.writeValueAsString(options.getClasses()));
        } catch (JsonProcessingException e) {
            throw new ApiException("거래소 옵션을 저장하는 중 오류가 발생했습니다.", e);
        }
        marketOptionMetaRepository.save(meta);
    }

    private MarketOptionsResponse toOptionsResponse(MarketOptionMeta meta) {
        try {
            MarketOptionsResponse response = new MarketOptionsResponse();
            if (meta.getItemGradesJson() != null) {
                response.setItemGrades(objectMapper.readValue(meta.getItemGradesJson(), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            if (meta.getItemTiersJson() != null) {
                response.setItemTiers(objectMapper.readValue(meta.getItemTiersJson(), objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class)));
            }
            if (meta.getClassesJson() != null) {
                response.setClasses(objectMapper.readValue(meta.getClassesJson(), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            return response;
        } catch (Exception e) {
            throw new ApiException("저장된 거래소 옵션을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
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

    public MarketItemDto refreshSingleItem(Long apiItemId, MarketItemRefreshRequest request) {
        if (apiItemId == null) {
            throw new ApiException("apiItemId가 필요합니다.");
        }
        if (request == null || request.getCategoryCode() == null) {
            throw new ApiException("categoryCode가 필요합니다.");
        }
        long now = System.currentTimeMillis();
        Long last = itemRefreshLocks.get(apiItemId);
        if (last != null && now - last < ITEM_REFRESH_COOLDOWN_MS) {
            throw new ApiException("아이템 요청이 너무 잦습니다. 잠시 후 다시 시도해 주세요.");
        }
        itemRefreshLocks.put(apiItemId, now);

        MarketItemsRequest apiRequest = MarketItemsRequest.builder()
                .sort(request.getSort() != null ? request.getSort() : "RECENT_PRICE")
                .sortCondition(request.getSortCondition() != null ? request.getSortCondition() : "ASC")
                .categoryCode(request.getCategoryCode())
                .characterClass(request.getCharacterClass())
                .itemName(request.getItemName())
                .itemTier(request.getItemTier())
                .itemGrade(request.getItemGrade())
                .pageNo(1)
                .pageSize(request.getPageSize() != null ? request.getPageSize() : 10)
                .build();

        MarketItemsResponse response = fetchMarketItemsWithRetry(apiRequest, request.getCategoryCode(), 1);
        if (response == null || CollectionUtils.isEmpty(response.getItems())) {
            throw new ApiException("아이템을 갱신하지 못했습니다.");
        }

        return response.getItems().stream()
                .filter(dto -> apiItemId.equals(dto.getId()))
                .findFirst()
                .orElse(response.getItems().get(0));
    }

    public MarketItemDetailDto getItemDetail(Long apiItemId) {
        try {
            List<MarketItemDetailDto> list = lostArkApiClient.getMarketItemDetail(apiItemId)
                    .blockOptional()
                    .orElse(Collections.emptyList());
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            // 동일 아이템이라도 거래 가능 여부 등에 따라 다중 응답이 내려오므로
            // 거래량/가격 데이터가 있는 쪽을 우선 선택한다.
            MarketItemDetailDto best = list.stream()
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingLong(this::detailScore))
                    .orElse(null);
            return best != null ? best : list.get(0);
        } catch (Exception e) {
            log.error("거래소 아이템 상세 조회 실패 - itemId={}", apiItemId, e);
            return null;
        }
    }

    private long detailScore(MarketItemDetailDto detail) {
        if (detail == null || CollectionUtils.isEmpty(detail.getStats())) {
            return Long.MIN_VALUE;
        }
        long tradeCountSum = detail.getStats().stream()
                .map(MarketItemStatDto::getTradeCount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long priceDays = detail.getStats().stream()
                .map(MarketItemStatDto::getAvgPrice)
                .filter(Objects::nonNull)
                .filter(price -> price > 0)
                .count();
        // 거래 건수가 많은 응답을 우선, 없으면 평균가가 있는 응답을 선택
        return tradeCountSum > 0 ? (tradeCountSum * 10 + priceDays) : priceDays;
    }

    private record PageBundle(List<MarketItemDto> items, Integer totalCount) {}
}
