package com.phobo.auth.dto;

// Put accessToken to LocalStorage or Cookie on client web
public record AuthResponse(
        String accessToken,
        String tokenType,
        String roleName
) {
    public AuthResponse(String accessToken, String roleName) {
        this(accessToken, "Bearer ", roleName);
    }
}