package com.lostark.backend.service;

import com.lostark.backend.dto.ArmoryDto;
import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.dto.SiblingCharacterDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class LostArkApiService {
    
    private final WebClient webClient;
    
    public LostArkApiService(
            @Value("${lostark.api.key}") String apiKey,
            @Value("${lostark.api.base-url}") String baseUrl,
            WebClient.Builder webClientBuilder) {
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
}
