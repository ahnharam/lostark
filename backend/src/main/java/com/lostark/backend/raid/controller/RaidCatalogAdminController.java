package com.lostark.backend.raid.controller;

import com.lostark.backend.raid.entity.RaidCatalog;
import com.lostark.backend.raid.repository.RaidCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/raid-catalog")
@RequiredArgsConstructor
public class RaidCatalogAdminController {

    private final RaidCatalogRepository raidCatalogRepository;

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
        catalog.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
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

        if (request.active() != null) {
            catalog.setActive(Boolean.TRUE.equals(request.active()));
        }

        RaidCatalog saved = raidCatalogRepository.save(catalog);
        return ResponseEntity.ok(RaidCatalogResponse.of(saved));
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public record RaidCatalogCreateRequest(String raidKey, String raidName, Boolean active) {}

    public record RaidCatalogUpdateRequest(String raidName, Boolean active) {}

    public record RaidCatalogResponse(String raidKey, String raidName, boolean active, String createdAt) {
        static RaidCatalogResponse of(RaidCatalog catalog) {
            return new RaidCatalogResponse(
                    catalog.getRaidKey(),
                    catalog.getRaidName(),
                    catalog.isActive(),
                    catalog.getCreatedAt() != null ? catalog.getCreatedAt().toString() : null
            );
        }
    }
}

