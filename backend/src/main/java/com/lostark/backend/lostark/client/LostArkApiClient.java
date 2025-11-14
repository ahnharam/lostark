package com.lostark.backend.lostark.client;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.dto.CollectibleDto;
import com.lostark.backend.dto.LeaderboardEntryDto;
import com.lostark.backend.dto.SiblingCharacterDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class LostArkApiClient {

    private final WebClient webClient;

    public LostArkApiClient(
            @Value("${lostark.api.key}") String apiKey,
            @Value("${lostark.api.base-url}") String baseUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("authorization", "bearer " + apiKey)
                .defaultHeader("accept", "application/json")
                .build();
    }

    public Mono<CharacterProfileDto> getCharacterProfile(String characterName) {
        return webClient.get()
                .uri("/armories/characters/{characterName}/profiles", characterName)
                .retrieve()
                .bodyToMono(CharacterProfileDto.class);
    }

    public Mono<ArmoryDto> getCharacterArmory(String characterName) {
        return webClient.get()
                .uri("/armories/characters/{characterName}", characterName)
                .retrieve()
                .bodyToMono(ArmoryDto.class);
    }

    public Mono<List<SiblingCharacterDto>> getSiblingCharacters(String characterName) {
        return webClient.get()
                .uri("/characters/{characterName}/siblings", characterName)
                .retrieve()
                .bodyToFlux(SiblingCharacterDto.class)
                .collectList();
    }

    public Mono<List<LeaderboardEntryDto>> getLeaderboardRankings(String leaderboardCode, String seasonId, int page) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/leaderboards/rankings")
                            .queryParam("leaderboardCode", leaderboardCode)
                            .queryParam("page", page);
                    if (seasonId != null && !seasonId.isBlank()) {
                        builder.queryParam("seasonId", seasonId);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToFlux(LeaderboardEntryDto.class)
                .collectList();
    }

    public Mono<List<CollectibleDto>> getCharacterCollectibles(String characterName) {
        return webClient.get()
                .uri("/armories/characters/{characterName}/collectibles", characterName)
                .retrieve()
                .bodyToFlux(CollectibleDto.class)
                .collectList();
    }
}
