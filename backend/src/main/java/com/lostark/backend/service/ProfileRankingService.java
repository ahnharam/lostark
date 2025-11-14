package com.lostark.backend.service;

import com.lostark.backend.dto.ProfileRankingResponseDto;
import com.lostark.backend.dto.RankingMetricDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.exception.CharacterNotFoundException;
import com.lostark.backend.repository.CharacterRepository;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProfileRankingService {

    private final CharacterRepository characterRepository;

    public ProfileRankingResponseDto getProfileRanking(String characterName) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new CharacterNotFoundException("캐릭터를 찾을 수 없습니다: " + characterName));

        List<Character> characters = characterRepository.findAll();
        Double itemLevelValue = parseNumber(character.getItemAvgLevel());
        Double expeditionValue = parseNumber(character.getExpeditionLevel());
        Double collectionValue = character.getCollectionScore();

        return ProfileRankingResponseDto.builder()
                .characterName(character.getCharacterName())
                .serverName(character.getServerName())
                .characterClassName(character.getCharacterClassName())
                .globalItemLevel(buildMetric(
                        characters,
                        c -> true,
                        c -> parseNumber(c.getItemAvgLevel()),
                        itemLevelValue,
                        "iLv"))
                .globalClassItemLevel(buildMetric(
                        characters,
                        c -> equalsIgnoreCase(c.getCharacterClassName(), character.getCharacterClassName()),
                        c -> parseNumber(c.getItemAvgLevel()),
                        itemLevelValue,
                        "iLv"))
                .serverItemLevel(buildMetric(
                        characters,
                        c -> equalsIgnoreCase(c.getServerName(), character.getServerName()),
                        c -> parseNumber(c.getItemAvgLevel()),
                        itemLevelValue,
                        "iLv"))
                .serverClassItemLevel(buildMetric(
                        characters,
                        c -> equalsIgnoreCase(c.getServerName(), character.getServerName()) &&
                                equalsIgnoreCase(c.getCharacterClassName(), character.getCharacterClassName()),
                        c -> parseNumber(c.getItemAvgLevel()),
                        itemLevelValue,
                        "iLv"))
                .expeditionLevel(buildMetric(
                        characters,
                        c -> true,
                        c -> parseNumber(c.getExpeditionLevel()),
                        expeditionValue,
                        "Lv"))
                .collectionScore(buildMetric(
                        characters,
                        c -> true,
                        Character::getCollectionScore,
                        collectionValue,
                        "pts"))
                .build();
    }

    private RankingMetricDto buildMetric(
            List<Character> characters,
            Predicate<Character> filter,
            Function<Character, Double> extractor,
            Double targetValue,
            String unit
    ) {
        if (targetValue == null) {
            return null;
        }

        long total = characters.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .count();

        if (total == 0) {
            return null;
        }

        long higherCount = characters.stream()
                .filter(filter)
                .map(extractor)
                .filter(Objects::nonNull)
                .filter(value -> value > targetValue)
                .count();

        int rank = (int) (higherCount + 1);
        double percentile = total > 0 ? (double) rank / total : 0d;

        return RankingMetricDto.builder()
                .rank(rank)
                .total((int) total)
                .metricValue(targetValue)
                .unit(unit)
                .percentile(percentile)
                .build();
    }

    private Double parseNumber(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            String normalized = raw.replaceAll("[^0-9.]", "");
            if (!StringUtils.hasText(normalized)) {
                return null;
            }
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }
}
