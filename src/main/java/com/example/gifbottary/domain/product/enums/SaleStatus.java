package com.example.gifbottary.domain.product.enums;

public enum SaleStatus {
    PENDING_REVIEW,  // 등록 직후 검수 대기
    ON_SALE,         // 판매 가능
    SOLD_OUT,        // 판매 완료
    CANCELLED,       // 판매 취소
    PIN_INVALID      // 핀 검수 실패
}
