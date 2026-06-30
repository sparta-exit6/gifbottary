package com.example.gifbottary.domain.product.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.ProductSearchPageResponse;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품/판매글 v2 검색 API 컨트롤러입니다.
 *
 * v2는 v1과 동일한 검색 조건을 지원하지만,
 * 서비스 레이어에서 로컬 캐시(Caffeine)를 적용해 응답 성능을 개선합니다.
 */
@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductV2Controller {

    private final ProductService productService;
    private final SearchService searchService;

    /**
     * 공개 판매글 목록을 조회합니다.
     * 비로그인 사용자도 접근할 수 있으며, 로그인한 경우에만 검색어를 저장합니다.
     *
     * v2는 동일한 검색 조건에 대해 캐시를 우선 조회합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProductSearchPageResponse>> findProducts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        searchService.saveSearchKeyword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(productService.searchProductsV2(request, pageable)));
    }
}