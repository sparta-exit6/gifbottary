package com.example.gifbottary.domain.search.controller;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.dto.response.RecentKeywordResponse;
import com.example.gifbottary.domain.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 검색과 검색어 캐시 기능을 제공하는 API입니다.
 * 현재는 인증 공통 객체가 아직 없어서 X-USER-ID 헤더를 임시 사용자 식별값으로 사용합니다.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private static final String USER_ID_HEADER = "X-USER-ID";

    private final SearchService searchService;
    private final ProductService productService;

    public SearchController(SearchService searchService, ProductService productService) {
        this.searchService = searchService;
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> searchProducts(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        searchService.saveSearchKeyword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(productService.findProducts(request, pageable)));
    }

    @GetMapping("/popular-keywords")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> findPopularKeywords(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.findPopularKeywords(limit)));
    }

    @GetMapping("/recent-keywords")
    public ResponseEntity<ApiResponse<List<RecentKeywordResponse>>> findRecentKeywords(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.findRecentKeywords(requireUserId(userId))));
    }

    @DeleteMapping("/recent-keywords/{keyword}")
    public ResponseEntity<ApiResponse<Void>> removeRecentKeyword(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @PathVariable String keyword
    ) {
        searchService.removeRecentKeyword(requireUserId(userId), keyword);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/recent-keywords")
    public ResponseEntity<ApiResponse<Void>> removeAllRecentKeywords(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId
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
