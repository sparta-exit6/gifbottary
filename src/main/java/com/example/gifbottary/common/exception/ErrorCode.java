package com.example.gifbottary.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 예외 코드를 관리합니다.
 */
@Getter
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON_004", "요청이 현재 상태와 충돌합니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_005", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_006", "이미 사용 중인 이메일입니다."),
    INVALID_LOGIN_INFO(HttpStatus.BAD_REQUEST, "AUTH_007", "이메일 또는 비밀번호가 올바르지 않습니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRODUCT_002", "재고가 부족합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "가격은 0 이상이어야 합니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_004", "재고는 0 이상이어야 합니다."),
    INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "PRODUCT_005", "재고 수량은 0보다 커야 합니다."),
    PRODUCT_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "PRODUCT_006", "본인 소유 상품만 수정할 수 있습니다."),
    PRODUCT_ALREADY_SOLD(HttpStatus.CONFLICT, "PRODUCT_007", "이미 판매된 핀이 포함된 상품은 삭제할 수 없습니다."),
    INVALID_SALE_STATUS(HttpStatus.BAD_REQUEST, "PRODUCT_008", "현재 상태에서는 요청한 판매 상태로 변경할 수 없습니다."),
    INVALID_EXPIRE_AT(HttpStatus.BAD_REQUEST, "PRODUCT_009", "유효기간은 오늘 이후여야 합니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.CONFLICT, "PRODUCT_010", "거래 가능한 상품이 아닙니다."),

    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_001", "핀 번호를 찾을 수 없습니다."),
    INVALID_PIN_INPUT(HttpStatus.BAD_REQUEST, "PIN_002", "핀 번호는 비어 있을 수 없습니다."),
    INVALID_PIN_COUNT(HttpStatus.BAD_REQUEST, "PIN_003", "핀 번호 개수가 판매 정책과 맞지 않습니다."),
    INVALID_PIN_STATUS(HttpStatus.BAD_REQUEST, "PIN_004", "요청한 핀 상태로 변경할 수 없습니다."),
    PIN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PIN_005", "핀 번호 암호화에 실패했습니다."),
    PIN_DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PIN_006", "핀 번호 복호화에 실패했습니다."),
    PIN_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "PIN_007", "핀 번호 검수에 실패했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),

    INVALID_QUERY_PARAMETER(HttpStatus.BAD_REQUEST, "SEARCH_001", "검색 조건이 올바르지 않습니다."),
    RECENT_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH_002", "최근 검색어를 찾을 수 없습니다."),
    POPULAR_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH_003", "인기 검색어를 찾을 수 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),
    ORDER_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "ORDER_002", "본인 주문만 조회할 수 있습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER_003", "현재 주문 상태에서는 요청을 처리할 수 없습니다."),

    PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "PURCHASE_001", "구매 내역을 찾을 수 없습니다."),
    PURCHASE_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "PURCHASE_002", "본인 구매 내역만 조회할 수 있습니다."),
    INVALID_PURCHASE_STATUS(HttpStatus.BAD_REQUEST, "PURCHASE_003", "현재 구매 상태에서는 요청을 처리할 수 없습니다."),

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "PAYMENT_002", "본인 결제만 조회할 수 있습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "PAYMENT_003", "결제 금액이 일치하지 않습니다."),
    PAYMENT_NOT_PAID_AT_PG(HttpStatus.CONFLICT, "PAYMENT_004", "PG 결제 상태가 완료가 아닙니다."),
    PAYMENT_ALREADY_FAILED(HttpStatus.CONFLICT, "PAYMENT_005", "이미 실패 처리된 결제입니다."),
    PAYMENT_ORDER_MISMATCH(HttpStatus.CONFLICT, "PAYMENT_006", "결제와 주문 정보가 일치하지 않습니다."),

    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "REFUND_001", "환불 정보를 찾을 수 없습니다."),
    REFUND_NOT_ALLOWED(HttpStatus.CONFLICT, "REFUND_002", "현재 상태에서는 환불할 수 없습니다."),
    REFUND_ALREADY_COMPLETED(HttpStatus.CONFLICT, "REFUND_003", "이미 환불 완료된 내역입니다."),

    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHATROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_002", "채팅방 접근 권한이 없습니다."),
    CANNOT_CHAT_WITH_SELF(HttpStatus.CONFLICT, "CHAT_003", "본인 상품에는 채팅을 시작할 수 없습니다."),
    INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "CHAT_004", "메시지 형식이 올바르지 않습니다."),
    ALREADY_EXITED_CHATROOM(HttpStatus.CONFLICT, "CHAT_005", "이미 나간 채팅방입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
