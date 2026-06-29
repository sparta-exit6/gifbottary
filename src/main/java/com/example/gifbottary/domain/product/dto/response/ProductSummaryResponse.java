package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;

import java.time.LocalDate;

public record ProductSummaryResponse(
        Long saleId,
        Long productId,
        SaleType saleType,
        String brand,
        String productName,
        Integer faceValue,
        LocalDate expireAt,
        Integer salePrice,
        Integer stock,
        SaleStatus saleStatus,
        String imageUrl
) {
}
