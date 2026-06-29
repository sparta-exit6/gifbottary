package com.example.gifbottary.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponIssueRequest(
        @NotNull(message = "쿠폰 ID는 필수입니다.") Long couponId
) {}
