package com.lostark.backend.service;

import com.lostark.backend.dto.SiblingCharacterDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiblingService {
    
    private final LostArkProfileDomainService lostArkProfileDomainService;
    
    public List<SiblingCharacterDto> getSiblingCharacters(String characterName) {
        return lostArkProfileDomainService.fetchSiblingCharacters(characterName);
    }
}
