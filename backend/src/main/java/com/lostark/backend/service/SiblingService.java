package com.lostark.backend.service;

import com.lostark.backend.dto.SiblingCharacterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiblingService {
    
    private final LostArkApiService lostArkApiService;
    
    public List<SiblingCharacterDto> getSiblingCharacters(String characterName) {
        return lostArkApiService.getSiblingCharacters(characterName).block();
    }
}
