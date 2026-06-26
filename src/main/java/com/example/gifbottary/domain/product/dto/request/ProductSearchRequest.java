package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;

public record ProductSearchRequest(
        String keyword,
        String brand,
        SaleType saleType,
        SaleStatus saleStatus,
        Integer minPrice,
        Integer maxPrice
) {

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasBrand() {
        return brand != null && !brand.isBlank();
    }
}