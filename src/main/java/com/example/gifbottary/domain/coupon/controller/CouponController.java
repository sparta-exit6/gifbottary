package com.example.gifbottary.domain.coupon.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.coupon.dto.request.CouponIssueRequest;
import com.example.gifbottary.domain.coupon.dto.response.CouponIssueResponse;
import com.example.gifbottary.domain.coupon.service.CouponService;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CouponIssueRequest request
    ) {
        CouponIssueResponse response = couponService.issueCoupon(request.couponId(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
