package com.example.gifbottary.domain.coupon.repository;

import com.example.gifbottary.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
