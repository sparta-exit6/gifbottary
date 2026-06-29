package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;

import java.time.LocalDateTime;

public record MyProductSummaryResponse(
        Long saleId,
        Long productId,
        SaleType saleType,
        String brand,
        String productName,
        Integer salePrice,
        SaleStatus saleStatus,
        LocalDateTime createdAt
) {
}
