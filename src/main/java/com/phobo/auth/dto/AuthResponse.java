package com.phobo.auth.dto;

// Put accessToken to LocalStorage or Cookie on client web
public record AuthResponse(
        String accessToken,
        String tokenType
) {
    public AuthResponse(String accessToken) {
        this(accessToken, "Bearer ");
    }
}