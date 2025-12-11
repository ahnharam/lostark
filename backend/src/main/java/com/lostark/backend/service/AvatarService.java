package com.lostark.backend.service;

import com.lostark.backend.dto.AvatarDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    public List<AvatarDto> getCharacterAvatars(String characterName, boolean force) {
        // force 플래그는 현재 캐시가 없으므로 무시되지만, 향후 확장 대비
        return lostArkProfileDomainService.fetchAvatars(characterName);
    }
}
