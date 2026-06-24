package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;

import java.time.LocalDate;

public record ProductSummaryResponse(
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
        String imageUrl
) {
    public static ProductSummaryResponse from(GifticonSale sale) {
        return new ProductSummaryResponse(
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
                sale.getProduct().getImageUrl()
        );
    }
}
