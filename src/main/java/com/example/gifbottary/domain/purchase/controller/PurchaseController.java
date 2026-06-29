package com.example.gifbottary.domain.purchase.controller;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.purchase.dto.response.PurchaseDetailResponse;
import com.example.gifbottary.domain.purchase.dto.response.PurchaseSummaryResponse;
import com.example.gifbottary.domain.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 구매 생성과 구매 내역 조회 API입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/sales/{saleId}")
    public ResponseEntity<ApiResponse<PurchaseDetailResponse>> createPurchase(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long saleId
    ) {
        PurchaseDetailResponse response = purchaseService.createPurchase(requireUserId(userId), saleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<PurchaseSummaryResponse>>> findMyPurchases(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.findMyPurchases(requireUserId(userId), pageable)));
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<ApiResponse<PurchaseDetailResponse>> findPurchase(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long purchaseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.findPurchase(requireUserId(userId), purchaseId)));
    }

    @PostMapping("/{purchaseId}/reveal-pin")
    public ResponseEntity<ApiResponse<PurchaseDetailResponse>> revealPin(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long purchaseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.revealPin(requireUserId(userId), purchaseId)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}