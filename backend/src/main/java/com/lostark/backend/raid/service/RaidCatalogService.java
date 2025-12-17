package com.lostark.backend.raid.service;

import com.lostark.backend.raid.entity.RaidCatalog;
import com.lostark.backend.raid.repository.RaidCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RaidCatalogService {

    private final RaidCatalogRepository raidCatalogRepository;

    /**
     * raidKey가 없을 때 raidName을 기준으로 DB(raid_catalog)에서 raidKey를 찾습니다.
     * <p>
     * 아직 카탈로그에 등록되지 않은 레이드도 생성 가능해야 하므로, 찾지 못하면 raidName을 fallback 키로 사용합니다.
     */
    public String resolveRaidKey(String raidKey, String raidName) {
        String normalizedKey = normalize(raidKey);
        if (normalizedKey != null) return normalizedKey;

        String normalizedName = normalize(raidName);
        if (normalizedName == null) return null;

        return raidCatalogRepository.findByRaidNameAndActiveTrue(normalizedName)
                .map(RaidCatalog::getRaidKey)
                .filter(key -> !key.isBlank())
                .orElse(normalizedName);
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
