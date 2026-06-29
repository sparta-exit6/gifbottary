package com.example.gifbottary.domain.coupon.service;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.coupon.dto.response.CouponIssueResponse;
import com.example.gifbottary.domain.coupon.entity.Coupon;
import com.example.gifbottary.domain.coupon.entity.UserCoupon;
import com.example.gifbottary.domain.coupon.repository.CouponRepository;
import com.example.gifbottary.domain.coupon.repository.UserCouponRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @Transactional
    public CouponIssueResponse issueCoupon(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new ServiceException(ErrorCode.COUPON_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new ServiceException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        coupon.issue();

        UserCoupon userCoupon = new UserCoupon(user, coupon);
        userCouponRepository.save(userCoupon);

        return CouponIssueResponse.from(coupon);
    }
}
