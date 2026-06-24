package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;

import java.time.LocalDateTime;

public record MyProductSummaryResponse(
        Long saleId,
        Long productId,
        String saleType,
        String brand,
        String productName,
        Integer salePrice,
        String saleStatus,
        LocalDateTime createdAt
) {
    public static MyProductSummaryResponse from(GifticonSale sale) {
        return new MyProductSummaryResponse(
                sale.getId(),
                sale.getProduct().getId(),
                sale.getSaleType().name(),
                sale.getProduct().getBrand(),
                sale.getProduct().getProductName(),
                sale.getSalePrice(),
                sale.getSaleStatus().name(),
                sale.getCreatedAt()
        );
    }
}
