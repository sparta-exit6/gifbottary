package com.example.gifbottary.domain.purchase.enums;

public enum PurchaseStatus {
    PENDING_PAYMENT, // 결제 대기
    PAID,       // 결제 완료
    CONFIRMED,  // 결제 확정
    REFUNDED    // 환불 완료
}
