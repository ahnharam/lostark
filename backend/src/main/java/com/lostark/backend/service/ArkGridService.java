package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.ArkGridResponseDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArkGridService {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    public ArkGridResponseDto getArkGrid(String characterName) {
        ArmoryDto armory = lostArkProfileDomainService.fetchArmory(characterName);
        ArkGridResponseDto response = new ArkGridResponseDto();
        response.setCharacterName(characterName);
        if (armory != null) {
            response.setArkPassive(armory.getArkPassive());
            response.setArkGrid(armory.getArkGrid());
        }
        return response;
    }
}
