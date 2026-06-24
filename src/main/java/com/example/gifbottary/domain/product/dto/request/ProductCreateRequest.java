package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleType;

import java.time.LocalDate;

public record ProductCreateRequest(
        SaleType saleType,
        String brand,
        String productName,
        Integer faceValue,
        LocalDate expireAt,
        Integer salePrice,
        String pinNumber,
        Integer stock,
        String imageUrl
) {
}