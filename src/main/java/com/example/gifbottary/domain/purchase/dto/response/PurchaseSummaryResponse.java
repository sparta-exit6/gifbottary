package com.example.gifbottary.domain.purchase.dto.response;

import com.example.gifbottary.domain.purchase.entity.Purchase;

import java.time.LocalDateTime;

/**
 * 구매 내역 목록 응답 DTO입니다.
 */
public record PurchaseSummaryResponse(
        Long purchaseId,
        Long saleId,
        String saleType,
        String brand,
        String productName,
        int quantity,
        int unitPrice,
        int totalPrice,
        String purchaseStatus,
        String pinStatus,
        Boolean refundLocked,
        LocalDateTime purchasedAt
) {

    public static PurchaseSummaryResponse from(Purchase purchase) {
        return new PurchaseSummaryResponse(
                purchase.getId(),
                purchase.getSale().getId(),
                purchase.getSale().getSaleType().name(),
                purchase.getSale().getProduct().getBrand(),
                purchase.getSale().getProduct().getProductName(),
                purchase.getQuantity(),
                purchase.getUnitPrice(),
                purchase.getTotalPrice(),
                purchase.getPurchaseStatus().name(),
                purchase.getPinStatus().name(),
                purchase.getRefundLocked(),
                purchase.getPurchasedAt()
        );
    }
}