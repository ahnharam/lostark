package com.lostark.backend.raid.bootstrap;

import com.lostark.backend.raid.entity.RaidCatalog;
import com.lostark.backend.raid.repository.RaidCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RaidCatalogBootstrap implements ApplicationRunner {

    private final RaidCatalogRepository raidCatalogRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<RaidSeed> seeds = defaultSeeds();

        for (RaidSeed seed : seeds) {
            if (raidCatalogRepository.existsById(seed.raidKey())) continue;
            if (raidCatalogRepository.findByRaidName(seed.raidName()).isPresent()) {
                // raidName은 unique 이므로, 동일 이름이 이미 등록되어 있으면 seed는 스킵합니다.
                continue;
            }

            RaidCatalog catalog = new RaidCatalog();
            catalog.setRaidKey(seed.raidKey());
            catalog.setRaidName(seed.raidName());
            catalog.setActive(true);
            raidCatalogRepository.save(catalog);
        }
    }

    private List<RaidSeed> defaultSeeds() {
        return List.of(
                new RaidSeed("epic-behemoth", "폭풍의 지휘관, 베히모스"),
                new RaidSeed("kazeros-prologue", "서막: 붉어진 백야의 나선"),
                new RaidSeed("kazeros-act-1", "1막: 대지를 부수는 업화의 궤적"),
                new RaidSeed("kazeros-act-2", "2막: 부유하는 악몽의 진혼곡"),
                new RaidSeed("kazeros-act-3", "3막: 침묵, 폭풍의 밤"),
                new RaidSeed("kazeros-act-4", "4막: 파멸의 성채"),
                new RaidSeed("kazeros-finale", "종막: 최후의 날")
        );
    }

    private record RaidSeed(String raidKey, String raidName) {}
}
