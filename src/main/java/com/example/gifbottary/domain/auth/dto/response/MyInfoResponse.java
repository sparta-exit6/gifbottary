package com.example.gifbottary.domain.auth.dto.response;

import com.example.gifbottary.domain.user.entity.User;

public record MyInfoResponse(
        Long userId,
        String email,
        String name,
        String role,
        int pointBalance
) {
    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getPointBalance()
        );
    }
}