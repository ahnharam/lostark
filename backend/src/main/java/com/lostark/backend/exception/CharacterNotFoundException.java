package com.lostark.backend.exception;

public class CharacterNotFoundException extends RuntimeException {
    public CharacterNotFoundException(String message) {
        super(message);
    }

    public CharacterNotFoundException(String characterName, Throwable cause) {
        super("캐릭터를 찾을 수 없습니다: " + characterName, cause);
    }
}
