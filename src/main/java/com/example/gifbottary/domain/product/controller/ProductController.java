package com.example.gifbottary.domain.product.controller;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.product.dto.request.*;
import com.example.gifbottary.domain.product.dto.response.*;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품/판매글 관리 API입니다.
 * 현재 인증 공통 객체가 없어 X-USER-ID 헤더를 임시 사용자 식별값으로 사용합니다.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    // TODO 회원API 구현 후 수정예정
    private static final String USER_ID = "X-USER-ID";

    private final ProductService productService;
    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateResponse response = productService.createProduct(requireUserId(sellerId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 공개 판매글 목록을 조회합니다.
     * 인증 없이도 전체 판매글을 조회할 수 있습니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> findProducts(
            @RequestHeader(value = USER_ID, required = false) Long userId,
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        searchService.saveSearchKeyword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(productService.findProducts(request, pageable)));
    }

    /**
     * 로그인 사용자의 판매글 목록을 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<MyProductSummaryResponse>>> findMyProducts(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findMyProducts(requireUserId(sellerId), request, pageable)));
    }

    @GetMapping("/{saleId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> findProduct(@PathVariable Long saleId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findProduct(saleId)));
    }

    @PatchMapping("/{saleId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @PathVariable Long saleId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(requireUserId(sellerId), saleId, request)));
    }

    @PatchMapping("/{saleId}/status")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateSaleStatus(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @PathVariable Long saleId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateSaleStatus(requireUserId(sellerId), saleId, request)));
    }

    @PatchMapping("/{saleId}/pins/{pinId}/validation")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updatePinValidationStatus(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @PathVariable Long saleId,
            @PathVariable Long pinId,
            @Valid @RequestBody PinValidationUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updatePinValidationStatus(requireUserId(sellerId), saleId, pinId, request)));
    }

    @GetMapping("/{saleId}/pin-validation")
    public ResponseEntity<ApiResponse<ProductPinValidationResponse>> findPinValidation(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @PathVariable Long saleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findPinValidation(requireUserId(sellerId), saleId)));
    }

    @DeleteMapping("/{saleId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(
            @RequestHeader(value = USER_ID, required = false) Long sellerId,
            @PathVariable Long saleId
    ) {
        productService.removeProduct(requireUserId(sellerId), saleId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
