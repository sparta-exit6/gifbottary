package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

/**
 * 판매글 수정 요청 DTO입니다.
 * 재고(stock)는 핀 엔티티 상태로 계산하므로 직접 수정하지 않습니다.
 */
public record ProductUpdateRequest(
        String productName,
        @Min(1) Integer faceValue,
        @Min(1) Integer salePrice,
        LocalDate expireAt,
        String description,
        SaleStatus saleStatus,
        String imageUrl,
        List<String> pinNumbers
) {
}
