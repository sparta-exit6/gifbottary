package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductDetailResponse(
        Long saleId,
        Long productId,
        Long sellerId,
        String saleType,
        String brand,
        String productName,
        Integer faceValue,
        LocalDate expireAt,
        Integer salePrice,
        Integer stock,
        String saleStatus,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDetailResponse from(GifticonSale sale) {
        return new ProductDetailResponse(
                sale.getId(),
                sale.getProduct().getId(),
                sale.getSeller().getId(),
                sale.getSaleType().name(),
                sale.getProduct().getBrand(),
                sale.getProduct().getProductName(),
                sale.getProduct().getFaceValue(),
                sale.getExpireAt(),
                sale.getSalePrice(),
                sale.getStock(),
                sale.getSaleStatus().name(),
                sale.getProduct().getImageUrl(),
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }
}
