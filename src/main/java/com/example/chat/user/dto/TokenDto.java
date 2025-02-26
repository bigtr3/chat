package com.example.chat.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenDto {
    private String accessToken;
    private String name;

    @Builder
    public TokenDto(String accessToken, String name) {
        this.accessToken = accessToken;
        this.name = name;
    }
}