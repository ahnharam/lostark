package com.lostark.backend.lostark.domain;

import com.lostark.backend.dto.LeaderboardEntryDto;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.lostark.client.LostArkApiClient;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardDomainService {

    private final LostArkApiClient lostArkApiClient;

    public List<LeaderboardEntryDto> fetchRankings(String leaderboardCode, String seasonId, int page) {
        try {
            return lostArkApiClient.getLeaderboardRankings(leaderboardCode, seasonId, page)
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("리더보드 데이터를 찾을 수 없습니다. code={} season={} page={}", leaderboardCode, seasonId, page);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ApiException("리더보드 데이터를 불러오는 중 오류가 발생했습니다.", e);
        }
    }
}
