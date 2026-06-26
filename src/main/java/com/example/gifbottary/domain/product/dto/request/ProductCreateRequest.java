package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 판매 등록 요청 DTO입니다.
 * 기존 상품을 선택하거나, 상품 정보를 직접 입력하는 두 가지 방식을 모두 지원합니다.
 */
public record ProductCreateRequest(
        Long productId,
        @NotNull SaleType saleType,
        String brand,
        String productName,
        @Min(1) Integer faceValue,
        @NotNull LocalDate expireAt,
        @NotNull @Min(1) Integer salePrice,
        String pinNumber,
        List<String> pinNumbers,
        String imageUrl
) {
}
