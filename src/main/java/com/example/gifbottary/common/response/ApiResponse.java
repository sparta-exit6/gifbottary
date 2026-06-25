package com.example.gifbottary.common.response;

import com.example.gifbottary.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

/**
 * 성공/실패 응답을 하나의 형식으로 통합한 공통 응답 객체입니다.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "message", "errorCode", "data"})
public class ApiResponse<T> {

    private final boolean success;   // 성공 여부 (true / false)
    private final String message;    // 응답 메시지
    private final String errorCode;  // 에러 코드 (실패 시에만 노출)
    private final T data;            // 실제 데이터 (성공 시에만 노출)

    private ApiResponse(boolean success, String message, String errorCode, T data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    // 성공 응답 ─────────────────────────────────────────────────────────────────────────────────

    // 데이터를 담아서 반환할 때 사용
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "요청이 성공했습니다.", null, data);
    }

    // 담을 데이터가 없을 때 사용
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "요청이 성공했습니다.", null, null);
    }

    // 커스텀 완료 메시지만 보낼 때 사용
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    // 실패 응답 ─────────────────────────────────────────────────────────────────────────────────

    // 공통 ErrorCode 객체를 이용해 에러를 반환할 때 사용
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getMessage(), errorCode.getCode(), null);
    }

    // 에러 메시지를 커스텀해서 반환할 때 사용
    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, message, errorCode.getCode(), null);
    }

    // 예외 처리기 등에서 문자열로 직접 에러를 주입할 때 사용
    public static ApiResponse<Void> error(String errorCode, String message) {
        return new ApiResponse<>(false, message, errorCode, null);
    }
}
