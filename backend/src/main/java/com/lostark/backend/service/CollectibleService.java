package com.lostark.backend.service;

import com.lostark.backend.dto.CollectibleDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectibleService {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    public List<CollectibleDto> getCollectibles(String characterName) {
        return lostArkProfileDomainService.fetchCollectibles(characterName);
    }
}
