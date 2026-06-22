package com.example.gifbottary.common.response;

import lombok.Builder;

@Builder
public record CommonResponse<T>(
    boolean success,
    String message,
    T data
) {
    // 공통 응답 빌더 CommonResponse.success(dto)
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
            .success(true)
            .message("요청이 성공했습니다.")
            .data(data)
            .build();
    }

    // 요청이 성공했습니다. 이외의 메시지 보내고 싶을 때 사용
    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }
}
