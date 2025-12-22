package com.lostark.backend.raid.controller;

import com.lostark.backend.raid.entity.RaidCatalog;
import com.lostark.backend.raid.repository.RaidCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/raid-catalog")
@RequiredArgsConstructor
public class RaidCatalogAdminController {

    private final RaidCatalogRepository raidCatalogRepository;
    private static final List<String> DIFFICULTY_ORDER = List.of("single", "normal", "hard", "nightmare");
    private static final Set<Integer> ALLOWED_PARTY_SIZES = Set.of(4, 8, 16);

    @GetMapping
    public ResponseEntity<List<RaidCatalogResponse>> list() {
        List<RaidCatalogResponse> result = raidCatalogRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(RaidCatalogResponse::of)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<RaidCatalogResponse> create(@RequestBody RaidCatalogCreateRequest request) {
        String raidKey = normalize(request.raidKey());
        String raidName = normalize(request.raidName());
        if (raidKey == null) throw new IllegalArgumentException("raidKey가 필요합니다.");
        if (raidName == null) throw new IllegalArgumentException("raidName이 필요합니다.");

        if (raidCatalogRepository.existsById(raidKey)) {
            throw new IllegalArgumentException("이미 존재하는 raidKey 입니다.");
        }
        raidCatalogRepository.findByRaidName(raidName).ifPresent(existing -> {
            throw new IllegalArgumentException("이미 존재하는 raidName 입니다.");
        });

        RaidCatalog catalog = new RaidCatalog();
        catalog.setRaidKey(raidKey);
        catalog.setRaidName(raidName);
        catalog.setAbbreviation(normalize(request.abbreviation()));
        catalog.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
        catalog.setItemLevel(request.itemLevel());
        catalog.setGoldReward(request.goldReward());
        List<String> normalizedDifficulties = normalizeDifficulties(request.difficulties());
        catalog.setDifficultyCodes(joinDifficulties(normalizedDifficulties));
        catalog.setItemLevelSingle(normalizeItemLevel(request.itemLevelSingle()));
        catalog.setItemLevelNormal(normalizeItemLevel(request.itemLevelNormal()));
        catalog.setItemLevelHard(normalizeItemLevel(request.itemLevelHard()));
        catalog.setItemLevelNightmare(normalizeItemLevel(request.itemLevelNightmare()));
        catalog.setGoldSingle(normalizeGold(request.goldSingle()));
        catalog.setGoldSingleTrade(normalizeGold(request.goldSingleTrade()));
        catalog.setGoldSingleBound(normalizeGold(request.goldSingleBound()));
        catalog.setGoldNormal(normalizeGold(request.goldNormal()));
        catalog.setGoldNormalTrade(normalizeGold(request.goldNormalTrade()));
        catalog.setGoldNormalBound(normalizeGold(request.goldNormalBound()));
        catalog.setGoldHard(normalizeGold(request.goldHard()));
        catalog.setGoldHardTrade(normalizeGold(request.goldHardTrade()));
        catalog.setGoldHardBound(normalizeGold(request.goldHardBound()));
        catalog.setGoldNightmare(normalizeGold(request.goldNightmare()));
        catalog.setGoldNightmareTrade(normalizeGold(request.goldNightmareTrade()));
        catalog.setGoldNightmareBound(normalizeGold(request.goldNightmareBound()));
        clearMissingDifficultyLevels(catalog, normalizedDifficulties);
        clearMissingDifficultyGolds(catalog, normalizedDifficulties);
        catalog.setPartySize(normalizePartySize(request.partySize()));
        RaidCatalog saved = raidCatalogRepository.save(catalog);
        return ResponseEntity.ok(RaidCatalogResponse.of(saved));
    }

    @PatchMapping("/{raidKey}")
    public ResponseEntity<RaidCatalogResponse> update(
            @PathVariable String raidKey,
            @RequestBody RaidCatalogUpdateRequest request
    ) {
        String normalizedKey = normalize(raidKey);
        if (normalizedKey == null) throw new IllegalArgumentException("raidKey가 필요합니다.");

        RaidCatalog catalog = raidCatalogRepository.findById(normalizedKey)
                .orElseThrow(() -> new IllegalArgumentException("raidKey에 해당하는 레이드를 찾을 수 없습니다."));

        String nextName = request.raidName() == null ? null : normalize(request.raidName());
        if (nextName != null && !Objects.equals(nextName, catalog.getRaidName())) {
            raidCatalogRepository.findByRaidName(nextName).ifPresent(existing -> {
                throw new IllegalArgumentException("이미 존재하는 raidName 입니다.");
            });
            catalog.setRaidName(nextName);
        }

        if (request.abbreviation() != null) {
            catalog.setAbbreviation(normalize(request.abbreviation()));
        }
        if (request.active() != null) {
            catalog.setActive(Boolean.TRUE.equals(request.active()));
        }
        if (request.itemLevel() != null) {
            catalog.setItemLevel(request.itemLevel());
        }
        if (request.goldReward() != null) {
            catalog.setGoldReward(request.goldReward());
        }
        List<String> normalizedDifficulties = null;
        if (request.difficulties() != null) {
            normalizedDifficulties = normalizeDifficulties(request.difficulties());
            catalog.setDifficultyCodes(joinDifficulties(normalizedDifficulties));
        }
        if (request.partySize() != null) {
            catalog.setPartySize(normalizePartySize(request.partySize()));
        }
        if (request.itemLevelSingle() != null) {
            catalog.setItemLevelSingle(normalizeItemLevel(request.itemLevelSingle()));
        }
        if (request.itemLevelNormal() != null) {
            catalog.setItemLevelNormal(normalizeItemLevel(request.itemLevelNormal()));
        }
        if (request.itemLevelHard() != null) {
            catalog.setItemLevelHard(normalizeItemLevel(request.itemLevelHard()));
        }
        if (request.itemLevelNightmare() != null) {
            catalog.setItemLevelNightmare(normalizeItemLevel(request.itemLevelNightmare()));
        }
        if (request.goldSingle() != null) {
            catalog.setGoldSingle(normalizeGold(request.goldSingle()));
        }
        if (request.goldSingleTrade() != null) {
            catalog.setGoldSingleTrade(normalizeGold(request.goldSingleTrade()));
        }
        if (request.goldSingleBound() != null) {
            catalog.setGoldSingleBound(normalizeGold(request.goldSingleBound()));
        }
        if (request.goldNormal() != null) {
            catalog.setGoldNormal(normalizeGold(request.goldNormal()));
        }
        if (request.goldNormalTrade() != null) {
            catalog.setGoldNormalTrade(normalizeGold(request.goldNormalTrade()));
        }
        if (request.goldNormalBound() != null) {
            catalog.setGoldNormalBound(normalizeGold(request.goldNormalBound()));
        }
        if (request.goldHard() != null) {
            catalog.setGoldHard(normalizeGold(request.goldHard()));
        }
        if (request.goldHardTrade() != null) {
            catalog.setGoldHardTrade(normalizeGold(request.goldHardTrade()));
        }
        if (request.goldHardBound() != null) {
            catalog.setGoldHardBound(normalizeGold(request.goldHardBound()));
        }
        if (request.goldNightmare() != null) {
            catalog.setGoldNightmare(normalizeGold(request.goldNightmare()));
        }
        if (request.goldNightmareTrade() != null) {
            catalog.setGoldNightmareTrade(normalizeGold(request.goldNightmareTrade()));
        }
        if (request.goldNightmareBound() != null) {
            catalog.setGoldNightmareBound(normalizeGold(request.goldNightmareBound()));
        }
        if (normalizedDifficulties != null) {
            clearMissingDifficultyLevels(catalog, normalizedDifficulties);
            clearMissingDifficultyGolds(catalog, normalizedDifficulties);
        }

        RaidCatalog saved = raidCatalogRepository.save(catalog);
        return ResponseEntity.ok(RaidCatalogResponse.of(saved));
    }

    @DeleteMapping("/{raidKey}")
    public ResponseEntity<Void> delete(@PathVariable String raidKey) {
        String normalizedKey = normalize(raidKey);
        if (normalizedKey == null) throw new IllegalArgumentException("raidKey가 필요합니다.");
        if (!raidCatalogRepository.existsById(normalizedKey)) {
            throw new IllegalArgumentException("raidKey에 해당하는 레이드를 찾을 수 없습니다.");
        }
        raidCatalogRepository.deleteById(normalizedKey);
        return ResponseEntity.noContent().build();
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static List<String> normalizeDifficulties(List<String> values) {
        if (values == null) return List.of();
        Set<String> selected = new HashSet<>();
        for (String value : values) {
            if (value == null) continue;
            String normalized = value.trim().toLowerCase();
            if (normalized.isBlank()) continue;
            if (DIFFICULTY_ORDER.contains(normalized)) {
                selected.add(normalized);
            }
        }
        List<String> ordered = new ArrayList<>();
        for (String difficulty : DIFFICULTY_ORDER) {
            if (selected.contains(difficulty)) {
                ordered.add(difficulty);
            }
        }
        return ordered;
    }

    private static String joinDifficulties(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return String.join(",", values);
    }

    private static List<String> splitDifficulties(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isBlank())
                .toList();
    }

    private static Integer normalizePartySize(Integer value) {
        if (value == null) return null;
        if (!ALLOWED_PARTY_SIZES.contains(value)) {
            throw new IllegalArgumentException("인원은 4, 8, 16만 입력 가능합니다.");
        }
        return value;
    }

    private static Integer normalizeGold(Integer value) {
        if (value == null || value <= 0) return null;
        return value;
    }

    private static Integer normalizeItemLevel(Integer value) {
        if (value == null || value <= 0) return null;
        return value;
    }

    private static void clearMissingDifficultyLevels(RaidCatalog catalog, List<String> difficulties) {
        if (difficulties == null || difficulties.isEmpty()) {
            catalog.setItemLevelSingle(null);
            catalog.setItemLevelNormal(null);
            catalog.setItemLevelHard(null);
            catalog.setItemLevelNightmare(null);
            return;
        }
        if (!difficulties.contains("single")) {
            catalog.setItemLevelSingle(null);
        }
        if (!difficulties.contains("normal")) {
            catalog.setItemLevelNormal(null);
        }
        if (!difficulties.contains("hard")) {
            catalog.setItemLevelHard(null);
        }
        if (!difficulties.contains("nightmare")) {
            catalog.setItemLevelNightmare(null);
        }
    }

    private static void clearMissingDifficultyGolds(RaidCatalog catalog, List<String> difficulties) {
        if (difficulties == null || difficulties.isEmpty()) {
            catalog.setGoldSingle(null);
            catalog.setGoldSingleTrade(null);
            catalog.setGoldSingleBound(null);
            catalog.setGoldNormal(null);
            catalog.setGoldNormalTrade(null);
            catalog.setGoldNormalBound(null);
            catalog.setGoldHard(null);
            catalog.setGoldHardTrade(null);
            catalog.setGoldHardBound(null);
            catalog.setGoldNightmare(null);
            catalog.setGoldNightmareTrade(null);
            catalog.setGoldNightmareBound(null);
            return;
        }
        if (!difficulties.contains("single")) {
            catalog.setGoldSingle(null);
            catalog.setGoldSingleTrade(null);
            catalog.setGoldSingleBound(null);
        }
        if (!difficulties.contains("normal")) {
            catalog.setGoldNormal(null);
            catalog.setGoldNormalTrade(null);
            catalog.setGoldNormalBound(null);
        }
        if (!difficulties.contains("hard")) {
            catalog.setGoldHard(null);
            catalog.setGoldHardTrade(null);
            catalog.setGoldHardBound(null);
        }
        if (!difficulties.contains("nightmare")) {
            catalog.setGoldNightmare(null);
            catalog.setGoldNightmareTrade(null);
            catalog.setGoldNightmareBound(null);
        }
    }

    public record RaidCatalogCreateRequest(
            String raidKey,
            String raidName,
            String abbreviation,
            Boolean active,
            Integer itemLevel,
            Integer goldReward,
            List<String> difficulties,
            Integer partySize,
            Integer itemLevelSingle,
            Integer itemLevelNormal,
            Integer itemLevelHard,
            Integer itemLevelNightmare,
            Integer goldSingle,
            Integer goldSingleTrade,
            Integer goldSingleBound,
            Integer goldNormal,
            Integer goldNormalTrade,
            Integer goldNormalBound,
            Integer goldHard,
            Integer goldHardTrade,
            Integer goldHardBound,
            Integer goldNightmare,
            Integer goldNightmareTrade,
            Integer goldNightmareBound
    ) {}

    public record RaidCatalogUpdateRequest(
            String raidName,
            String abbreviation,
            Boolean active,
            Integer itemLevel,
            Integer goldReward,
            List<String> difficulties,
            Integer partySize,
            Integer itemLevelSingle,
            Integer itemLevelNormal,
            Integer itemLevelHard,
            Integer itemLevelNightmare,
            Integer goldSingle,
            Integer goldSingleTrade,
            Integer goldSingleBound,
            Integer goldNormal,
            Integer goldNormalTrade,
            Integer goldNormalBound,
            Integer goldHard,
            Integer goldHardTrade,
            Integer goldHardBound,
            Integer goldNightmare,
            Integer goldNightmareTrade,
            Integer goldNightmareBound
    ) {}

    public record RaidCatalogResponse(
            String raidKey,
            String raidName,
            String abbreviation,
            boolean active,
            Integer itemLevel,
            Integer goldReward,
            List<String> difficulties,
            Integer partySize,
            Integer itemLevelSingle,
            Integer itemLevelNormal,
            Integer itemLevelHard,
            Integer itemLevelNightmare,
            Integer goldSingle,
            Integer goldSingleTrade,
            Integer goldSingleBound,
            Integer goldNormal,
            Integer goldNormalTrade,
            Integer goldNormalBound,
            Integer goldHard,
            Integer goldHardTrade,
            Integer goldHardBound,
            Integer goldNightmare,
            Integer goldNightmareTrade,
            Integer goldNightmareBound,
            String createdAt
    ) {
        static RaidCatalogResponse of(RaidCatalog catalog) {
            return new RaidCatalogResponse(
                    catalog.getRaidKey(),
                    catalog.getRaidName(),
                    catalog.getAbbreviation(),
                    catalog.isActive(),
                    catalog.getItemLevel(),
                    catalog.getGoldReward(),
                    splitDifficulties(catalog.getDifficultyCodes()),
                    catalog.getPartySize(),
                    catalog.getItemLevelSingle(),
                    catalog.getItemLevelNormal(),
                    catalog.getItemLevelHard(),
                    catalog.getItemLevelNightmare(),
                    catalog.getGoldSingle(),
                    catalog.getGoldSingleTrade(),
                    catalog.getGoldSingleBound(),
                    catalog.getGoldNormal(),
                    catalog.getGoldNormalTrade(),
                    catalog.getGoldNormalBound(),
                    catalog.getGoldHard(),
                    catalog.getGoldHardTrade(),
                    catalog.getGoldHardBound(),
                    catalog.getGoldNightmare(),
                    catalog.getGoldNightmareTrade(),
                    catalog.getGoldNightmareBound(),
                    catalog.getCreatedAt() != null ? catalog.getCreatedAt().toString() : null
            );
        }
    }
}
