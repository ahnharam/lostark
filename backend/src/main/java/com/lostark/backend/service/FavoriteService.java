package com.lostark.backend.service;

import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.entity.Character;
import com.lostark.backend.entity.Favorite;
import com.lostark.backend.repository.CharacterRepository;
import com.lostark.backend.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final CharacterRepository characterRepository;
    
    @Transactional
    public void addFavorite(String userId, String characterName) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        
        // 이미 즐겨찾기에 있는지 확인
        if (favoriteRepository.findByUserIdAndCharacterId(userId, character.getId()).isPresent()) {
            return;
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCharacter(character);
        favoriteRepository.save(favorite);
    }
    
    @Transactional
    public void removeFavorite(String userId, String characterName) {
        Character character = characterRepository.findByCharacterName(characterName)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        
        favoriteRepository.deleteByUserIdAndCharacterId(userId, character.getId());
    }
    
    public List<CharacterProfileDto> getFavorites(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        
        return favorites.stream()
                .map(favorite -> convertToDto(favorite.getCharacter()))
                .collect(Collectors.toList());
    }
    
    public boolean isFavorite(String userId, String characterName) {
        Character character = characterRepository.findByCharacterName(characterName).orElse(null);
        if (character == null) {
            return false;
        }
        return favoriteRepository.findByUserIdAndCharacterId(userId, character.getId()).isPresent();
    }
    
    private CharacterProfileDto convertToDto(Character character) {
        CharacterProfileDto dto = new CharacterProfileDto();
        dto.setCharacterName(character.getCharacterName());
        dto.setServerName(character.getServerName());
        dto.setCharacterClassName(character.getCharacterClassName());
        dto.setItemAvgLevel(character.getItemAvgLevel());
        dto.setItemMaxLevel(character.getItemMaxLevel());
        dto.setCharacterImage(character.getCharacterImage());
        dto.setExpeditionLevel(character.getExpeditionLevel() != null ? 
                Integer.parseInt(character.getExpeditionLevel()) : null);
        dto.setPvpGradeName(character.getPvpGradeName());
        dto.setGuildName(character.getGuildName());
        return dto;
    }
}
