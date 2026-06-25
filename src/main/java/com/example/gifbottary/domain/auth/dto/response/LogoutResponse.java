package com.example.gifbottary.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutResponse {

    private String message;

    public static LogoutResponse from() {
        return LogoutResponse.builder()
                .message("로그아웃이 완료되었습니다.")
                .build();
    }
}