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

    public static LoginResponse from(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .pointBalance(user.getPointBalance())
                .build();
    }
}
