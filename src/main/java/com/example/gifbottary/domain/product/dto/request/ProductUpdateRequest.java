package com.example.gifbottary.domain.product.dto.request;

import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 판매글 수정 요청 DTO입니다.
 * 재고(stock)는 핀 엔티티 상태로 계산하므로 직접 수정하지 않습니다.
 */
public record ProductUpdateRequest(
        @Min(1) Integer salePrice,
        String imageUrl,
        List<String> pinNumbers
) {
}
