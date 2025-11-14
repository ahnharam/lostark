package com.lostark.backend.service;

import com.lostark.backend.dto.LeaderboardEntryDto;
import com.lostark.backend.dto.RankingHighlightDto;
import com.lostark.backend.dto.RankingResponseDto;
import com.lostark.backend.dto.RankingSummaryDto;
import com.lostark.backend.lostark.domain.LeaderboardDomainService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final long CACHE_TTL_MILLIS = 5 * 60 * 1000L;
    private static final int MAX_HIGHLIGHT_SEARCH_PAGES = 3;
    private static final Set<String> ALLOWED_LEADERBOARD_CODES = Set.of("0101", "0201", "0202", "0203");

    private final LeaderboardDomainService leaderboardDomainService;
    private final Map<String, CachedPage> cache = new ConcurrentHashMap<>();

    public RankingResponseDto getRankings(String leaderboardCode, String seasonId, Integer page, String characterName) {
        validateLeaderboardCode(leaderboardCode);
        int resolvedPage = (page == null || page < 1) ? 1 : page;
        List<LeaderboardEntryDto> entries = fetchEntries(leaderboardCode, seasonId, resolvedPage);
        RankingHighlightDto highlight = resolveHighlight(characterName, leaderboardCode, seasonId);
        String resolvedSeason = StringUtils.hasText(seasonId) ? seasonId : inferSeasonId(entries);

        RankingSummaryDto summary = RankingSummaryDto.builder()
                .leaderboardCode(leaderboardCode)
                .seasonId(resolvedSeason)
                .page(resolvedPage)
                .totalFetched(entries.size())
                .lastUpdated(resolveLastUpdated(entries))
                .highlightedCharacter(highlight)
                .build();

        return RankingResponseDto.builder()
                .summary(summary)
                .entries(entries)
                .build();
    }

    private void validateLeaderboardCode(String leaderboardCode) {
        if (!ALLOWED_LEADERBOARD_CODES.contains(leaderboardCode)) {
            throw new IllegalArgumentException("지원하지 않는 리더보드 코드입니다.");
        }
    }

    private RankingHighlightDto resolveHighlight(String characterName, String leaderboardCode, String seasonId) {
        if (!StringUtils.hasText(characterName)) {
            return null;
        }

        for (int page = 1; page <= MAX_HIGHLIGHT_SEARCH_PAGES; page++) {
            List<LeaderboardEntryDto> entries = fetchEntries(leaderboardCode, seasonId, page);
            Optional<LeaderboardEntryDto> match = entries.stream()
                    .filter(entry -> characterName.equalsIgnoreCase(entry.getCharacterName()))
                    .findFirst();
            if (match.isPresent()) {
                LeaderboardEntryDto entry = match.get();
                return RankingHighlightDto.builder()
                        .characterName(entry.getCharacterName())
                        .serverName(entry.getServerName())
                        .characterClassName(entry.getCharacterClassName())
                        .rank(entry.getRank())
                        .rating(entry.getRating())
                        .itemAvgLevel(entry.getItemAvgLevel())
                        .tier(entry.getTier())
                        .division(entry.getDivision())
                        .build();
            }
        }
        return null;
    }

    private List<LeaderboardEntryDto> fetchEntries(String leaderboardCode, String seasonId, int page) {
        String cacheKey = buildCacheKey(leaderboardCode, seasonId, page);
        CachedPage cached = cache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.fetchedAt <= CACHE_TTL_MILLIS) {
            return cached.entries();
        }

        List<LeaderboardEntryDto> entries = leaderboardDomainService.fetchRankings(leaderboardCode, seasonId, page);
        cache.put(cacheKey, new CachedPage(entries, now));
        return entries;
    }

    private String buildCacheKey(String leaderboardCode, String seasonId, int page) {
        return leaderboardCode + ":" + (seasonId == null ? "" : seasonId) + ":" + page;
    }

    private String resolveLastUpdated(List<LeaderboardEntryDto> entries) {
        return entries.stream()
                .map(LeaderboardEntryDto::getUpdatedDate)
                .filter(StringUtils::hasText)
                .map(this::safeParseInstant)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .map(Instant::toString)
                .orElse(null);
    }

    private Instant safeParseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String inferSeasonId(List<LeaderboardEntryDto> entries) {
        return entries.stream()
                .map(LeaderboardEntryDto::getSeasonId)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private record CachedPage(List<LeaderboardEntryDto> entries, long fetchedAt) {}
}
