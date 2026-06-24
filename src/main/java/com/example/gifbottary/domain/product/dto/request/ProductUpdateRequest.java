package com.example.gifbottary.domain.product.dto.request;

import java.time.LocalDate;

public record ProductUpdateRequest(
        Integer salePrice,
        LocalDate expireAt,
        Integer stock,
        String imageUrl
) {
}
