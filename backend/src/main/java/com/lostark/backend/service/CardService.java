package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.CardResponseDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

    private final LostArkProfileDomainService lostArkProfileDomainService;

    public CardResponseDto getCards(String characterName) {
        ArmoryDto armory = lostArkProfileDomainService.fetchArmory(characterName);
        CardResponseDto response = new CardResponseDto();
        response.setCharacterName(characterName);
        if (armory != null) {
            response.setCard(armory.getCard());
        }
        return response;
    }
}
