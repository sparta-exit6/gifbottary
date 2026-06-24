package com.example.gifbottary.domain.auth.dto.response;

import com.example.gifbottary.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userId;
    private String email;
    private String name;
    private String role;
    private int pointBalance;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .pointBalance(user.getPointBalance())
                .build();
    }

}
