package com.example.gifbottary.domain.auth.dto.response;

import com.example.gifbottary.domain.user.entity.User;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        String role
) {

    public static SignupResponse from(User user) {

        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }
}
