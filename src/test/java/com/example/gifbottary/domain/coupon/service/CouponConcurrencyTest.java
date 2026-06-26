package com.example.gifbottary.domain.coupon.service;

import com.example.gifbottary.domain.coupon.entity.Coupon;
import com.example.gifbottary.domain.coupon.repository.CouponRepository;
import com.example.gifbottary.domain.coupon.repository.UserCouponRepository;
import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    private Long couponId;
    private final List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();

        Coupon coupon = new Coupon("선착순 10명 치킨 5천원 할인", 5000, 10);
        Coupon savedCoupon = couponRepository.save(coupon);
        couponId = savedCoupon.getId();

        userIds.clear();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(User.builder()
                    .email("concurrent_user_" + i + "@test.com")
                    .password("password")
                    .name("유저" + i)
                    .role(Role.USER)
                    .pointBalance(0)
                    .build());
        }
        List<User> savedUsers = userRepository.saveAll(users);
        savedUsers.forEach(user -> userIds.add(user.getId()));
    }

    @Test
    @DisplayName("선착순 10개 발급 동시성 검증 - 100명의 유저가 동시에 발급 API 호출 시 정확히 10장만 발급되어야 한다")
    void issueCoupon_concurrently_100users() throws InterruptedException {
        // given
        int threadCount = 100;
        try (ExecutorService executorService = Executors.newFixedThreadPool(32)) {
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                final Long userId = userIds.get(i);
                executorService.submit(() -> {
                    try {
                        couponService.issueCoupon(couponId, userId);
                    } catch (Exception e) {
                        // 수량 초과 시 예외 발생
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        // then
        long actualIssuedCount = userCouponRepository.count();
        Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();

        // 선착순 10개 한정이므로 정확히 10건만 발급되고 쿠폰 기록 수량도 10이어야 한다
        assertThat(actualIssuedCount).isEqualTo(10);
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10);
    }
}
