package com.example.gifbottary.domain.purchase.dto.response;

import com.example.gifbottary.domain.purchase.entity.Purchase;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 구매 상세 응답 DTO입니다.
 */
public record PurchaseDetailResponse(
        Long purchaseId,
        Long saleId,
        Long productId,
        String saleType,
        String brand,
        String productName,
        Integer faceValue,
        Integer salePrice,
        int quantity,
        int unitPrice,
        int totalPrice,
        LocalDate expireAt,
        String purchaseStatus,
        String pinStatus,
        Boolean refundLocked,
        String pinNumber,
        LocalDateTime purchasedAt,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PurchaseDetailResponse from(Purchase purchase, String pinNumber) {
        return new PurchaseDetailResponse(
                purchase.getId(),
                purchase.getSale().getId(),
                purchase.getSale().getProduct().getId(),
                purchase.getSale().getSaleType().name(),
                purchase.getSale().getProduct().getBrand(),
                purchase.getSale().getProduct().getProductName(),
                purchase.getSale().getProduct().getFaceValue(),
                purchase.getSale().getSalePrice(),
                purchase.getQuantity(),
                purchase.getUnitPrice(),
                purchase.getTotalPrice(),
                purchase.getSale().getExpireAt(),
                purchase.getPurchaseStatus().name(),
                purchase.getPinStatus().name(),
                purchase.getRefundLocked(),
                pinNumber,
                purchase.getPurchasedAt(),
                purchase.getConfirmedAt(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }
}