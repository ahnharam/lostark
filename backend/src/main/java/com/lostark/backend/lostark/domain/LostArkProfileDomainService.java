package com.lostark.backend.lostark.domain;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.dto.CombatSkillDto;
import com.lostark.backend.dto.CollectibleDto;
import com.lostark.backend.dto.EngravingResponseDto;
import com.lostark.backend.dto.SkillGemDto;
import com.lostark.backend.dto.SiblingCharacterDto;
import com.lostark.backend.exception.ApiException;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.lostark.client.LostArkApiClient;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostArkProfileDomainService {

    private final LostArkApiClient lostArkApiClient;

    public CharacterProfileDto fetchCharacterProfile(String characterName) {
        try {
            CharacterProfileDto profile = lostArkApiClient.getCharacterProfile(characterName)
                    .blockOptional()
                    .orElseThrow(() -> new CharacterNotFoundException("캐릭터를 찾을 수 없습니다: " + characterName));

            if (profile.getCombatPower() == null) {
                enrichCombatPower(characterName, profile);
            }

            return profile;
        } catch (CharacterNotFoundException e) {
            throw e;
        } catch (WebClientResponseException.NotFound e) {
            throw new CharacterNotFoundException(characterName, e);
        } catch (Exception e) {
            throw new ApiException("로스트아크 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public ArmoryDto fetchArmory(String characterName) {
        try {
            return lostArkApiClient.getCharacterArmory(characterName)
                    .blockOptional()
                    .orElse(null);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Armory 정보를 찾을 수 없습니다: {}", characterName);
            return null;
        } catch (Exception e) {
            throw new ApiException("로스트아크 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public List<SiblingCharacterDto> fetchSiblingCharacters(String characterName) {
        try {
            return lostArkApiClient.getSiblingCharacters(characterName)
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("원정대 캐릭터 정보를 찾을 수 없습니다: {}", characterName);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ApiException("로스트아크 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public List<CollectibleDto> fetchCollectibles(String characterName) {
        try {
            return lostArkApiClient.getCharacterCollectibles(characterName)
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("수집품 정보를 찾을 수 없습니다: {}", characterName);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ApiException("수집품 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    public List<CombatSkillDto> fetchCombatSkills(String characterName) {
        try {
            return lostArkApiClient.getCharacterCombatSkills(characterName)
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("전투 스킬 정보를 찾을 수 없습니다: {}", characterName);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ApiException("전투 스킬 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    public List<SkillGemDto> fetchSkillGems(String characterName) {
        try {
            return lostArkApiClient.getCharacterSkillGems(characterName)
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("스킬 보석 정보를 찾을 수 없습니다: {}", characterName);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ApiException("스킬 보석 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    public EngravingResponseDto fetchEngravings(String characterName) {
        try {
            return lostArkApiClient.getCharacterEngravings(characterName)
                    .blockOptional()
                    .orElse(null);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("각인 정보를 찾을 수 없습니다: {}", characterName);
            return null;
        } catch (Exception e) {
            throw new ApiException("각인 정보를 불러오는 중 오류가 발생했습니다.", e);
        }
    }

    private void enrichCombatPower(String characterName, CharacterProfileDto profile) {
        ArmoryDto armoryDto = fetchArmory(characterName);
        if (armoryDto == null || armoryDto.getProfile() == null) {
            return;
        }

        if (armoryDto.getProfile().getCombatPower() != null) {
            profile.setCombatPower(armoryDto.getProfile().getCombatPower());
        }
    }
}
