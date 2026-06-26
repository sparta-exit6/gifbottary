package com.example.gifbottary.domain.auth.dto.response;

public record LogoutResponse(
        String message
) {
    public static LogoutResponse from() {
        return new LogoutResponse("로그아웃이 완료되었습니다.");
    }
}