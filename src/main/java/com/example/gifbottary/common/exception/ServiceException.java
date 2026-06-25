package com.example.gifbottary.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외를 공통 ErrorCode와 함께 전달합니다.
 */
@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
