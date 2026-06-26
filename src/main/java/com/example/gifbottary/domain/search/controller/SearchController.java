package com.example.gifbottary.domain.search.controller;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.dto.response.RecentKeywordResponse;
import com.example.gifbottary.domain.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 검색과 검색어 캐시 기능을 제공하는 API입니다.
 * 현재는 인증 공통 객체가 아직 없어서 X-USER-ID 헤더를 임시 사용자 식별값으로 사용합니다.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;
    private final ProductService productService;

    public SearchController(SearchService searchService, ProductService productService) {
        this.searchService = searchService;
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> searchProducts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) SaleType saleType,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        ProductSearchRequest request = new ProductSearchRequest(keyword, brand, saleType, null, minPrice, maxPrice);
        searchService.saveSearchKeyword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(productService.findProducts(request, pageable)));
    }

    @GetMapping("/popular-keywords")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> findPopularKeywords(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.findPopularKeywordsV1(limit)));
    }

    @GetMapping("/recent-keywords")
    public ResponseEntity<ApiResponse<List<RecentKeywordResponse>>> findRecentKeywords(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.findRecentKeywords(requireUserId(userId))));
    }

    @DeleteMapping("/recent-keywords/{keyword}")
    public ResponseEntity<ApiResponse<Void>> removeRecentKeyword(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable String keyword
    ) {
        searchService.removeRecentKeyword(requireUserId(userId), keyword);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/recent-keywords")
    public ResponseEntity<ApiResponse<Void>> removeAllRecentKeywords(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        searchService.removeAllRecentKeywords(requireUserId(userId));
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
