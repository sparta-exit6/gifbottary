package com.example.gifbottary.domain.product.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 검색 결과 페이지를 Redis 캐시와 API 응답에서 안정적으로 사용하기 위한 DTO입니다.
 */
public record ProductSearchPageResponse(
        List<ProductSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static ProductSearchPageResponse from(Page<ProductSummaryResponse> page) {
        return new ProductSearchPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}