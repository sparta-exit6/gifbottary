package com.example.gifbottary.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType
) {

    public static LoginResponse from(String accessToken) {
        return new LoginResponse(
                accessToken,
                "Bearer"
        );
    }
}