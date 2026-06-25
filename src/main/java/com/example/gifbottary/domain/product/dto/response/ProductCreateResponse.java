package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 판매 등록 응답 DTO입니다.
 * 등록 직후에는 pinCheckStatus=PENDING, saleStatus=PENDING_REVIEW 상태를 반환합니다.
 */
public record ProductCreateResponse(
        Long saleId,
        Long productId,
        String saleType,
        String brand,
        String productName,
        Integer faceValue,
        LocalDate expireAt,
        Integer salePrice,
        Integer stock,
        String saleStatus,
        String pinCheckStatus,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static ProductCreateResponse from(GifticonSale sale) {
        String pinCheckStatus = sale.getPins().stream()
                .map(pin -> pin.getPinValidationStatus().name())
                .findFirst()
                .orElse(PinValidationStatus.PENDING.name());

        return new ProductCreateResponse(
                sale.getId(),
                sale.getProduct().getId(),
                sale.getSaleType().name(),
                sale.getProduct().getBrand(),
                sale.getProduct().getProductName(),
                sale.getProduct().getFaceValue(),
                sale.getExpireAt(),
                sale.getSalePrice(),
                sale.getStock(),
                sale.getSaleStatus().name(),
                pinCheckStatus,
                sale.getProduct().getImageUrl(),
                sale.getCreatedAt()
        );
    }
}
