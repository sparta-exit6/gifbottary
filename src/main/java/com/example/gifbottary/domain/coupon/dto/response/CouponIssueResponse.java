package com.example.gifbottary.domain.coupon.dto.response;

import com.example.gifbottary.domain.coupon.entity.Coupon;

public record CouponIssueResponse(
        String couponName,
        int discountPrice
) {
    public static CouponIssueResponse from(Coupon coupon) {
        return new CouponIssueResponse(
                coupon.getName(),
                coupon.getDiscountPrice()
        );
    }
}
