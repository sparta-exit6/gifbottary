package com.example.gifbottary.common.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ErrorResponse(
    boolean success,
    String message,
    HttpStatus errorCode
) {
    public static ErrorResponse of(String message, HttpStatus errorCode) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
}
