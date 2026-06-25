package com.example.gifbottary.domain.auth.dto.response;

import com.example.gifbottary.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String name;
    private String role;
    private int pointBalance;
    private String accessToken;
    private String tokenType;

    public static LoginResponse of(User user, String accessToken) {
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .pointBalance(user.getPointBalance())
                .accessToken(accessToken)
                .tokenType("Bearer")
                .build();
    }
}