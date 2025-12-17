package com.lostark.backend.raid.repository;

import com.lostark.backend.raid.entity.RaidCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RaidCatalogRepository extends JpaRepository<RaidCatalog, String> {

    Optional<RaidCatalog> findByRaidName(String raidName);

    Optional<RaidCatalog> findByRaidNameAndActiveTrue(String raidName);
}
