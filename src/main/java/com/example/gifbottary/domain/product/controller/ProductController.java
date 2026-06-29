package com.example.gifbottary.domain.product.controller;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.product.dto.request.PinValidationUpdateRequest;
import com.example.gifbottary.domain.product.dto.request.ProductCreateRequest;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.request.ProductStatusUpdateRequest;
import com.example.gifbottary.domain.product.dto.request.ProductUpdateRequest;
import com.example.gifbottary.domain.product.dto.response.MyProductSummaryResponse;
import com.example.gifbottary.domain.product.dto.response.ProductCreateResponse;
import com.example.gifbottary.domain.product.dto.response.ProductDetailResponse;
import com.example.gifbottary.domain.product.dto.response.ProductPinValidationResponse;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품/판매글 관리 API입니다.
 * 인증이 필요한 요청은 JWT principal에서 사용자 식별값을 꺼내 사용합니다.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateResponse response = productService.createProduct(requireUserId(sellerId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 공개 판매글 목록을 조회합니다.
     * 비로그인 사용자도 접근할 수 있으며, 로그인한 경우에만 검색어를 저장합니다.
     * v1은 캐시 없이 매 요청마다 DB를 조회합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> findProducts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        searchService.saveSearchKeyword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(productService.searchProductsV1(request, pageable)));
    }

    /**
     * 로그인한 사용자의 판매글 목록을 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<MyProductSummaryResponse>>> findMyProducts(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
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
            @AuthenticationPrincipal(expression = "id") Long sellerId,
            @PathVariable Long saleId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(requireUserId(sellerId), saleId, request)));
    }

    @PatchMapping("/{saleId}/status")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateSaleStatus(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
            @PathVariable Long saleId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateSaleStatus(requireUserId(sellerId), saleId, request)));
    }

    @PatchMapping("/{saleId}/pins/{pinId}/validation")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updatePinValidationStatus(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
            @PathVariable Long saleId,
            @PathVariable Long pinId,
            @Valid @RequestBody PinValidationUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updatePinValidationStatus(requireUserId(sellerId), saleId, pinId, request)));
    }

    @GetMapping("/{saleId}/pin-validation")
    public ResponseEntity<ApiResponse<ProductPinValidationResponse>> findPinValidation(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
            @PathVariable Long saleId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findPinValidation(requireUserId(sellerId), saleId)));
    }

    @DeleteMapping("/{saleId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(
            @AuthenticationPrincipal(expression = "id") Long sellerId,
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