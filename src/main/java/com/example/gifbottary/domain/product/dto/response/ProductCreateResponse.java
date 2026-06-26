package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 판매글 생성 직후 반환하는 응답입니다.
 * 핀 검수 상태는 여러 핀이 섞일 수 있으므로 생성 응답에 포함하지 않고,
 * 상세 조회나 핀 검수 조회 API에서 확인합니다.
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
        String imageUrl,
        LocalDateTime createdAt
) {
    public static ProductCreateResponse from(GifticonSale sale) {
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
                sale.getProduct().getImageUrl(),
                sale.getCreatedAt()
        );
    }
}
