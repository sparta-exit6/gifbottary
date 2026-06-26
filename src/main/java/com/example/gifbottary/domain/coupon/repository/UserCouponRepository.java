package com.example.gifbottary.domain.coupon.repository;

import com.example.gifbottary.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
