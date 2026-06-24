package com.example.gifbottary.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gifbottary.domain.order.entity.Order;
import com.example.gifbottary.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrder(Order order);

	//포트원 결제 검증 조회시 사용
	Optional<Payment> findByPortOnePaymentId(String portOnePaymentId);

	//특정 구매자의 결제 목록 최신순으로 가져오기
	List<Payment> findAllByOrder_Buyer_IdOrderByCreatedAtDesc(Long buyerId);
}
