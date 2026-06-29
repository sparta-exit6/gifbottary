package com.example.gifbottary.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gifbottary.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPortOnePaymentId(String portOnePaymentId);

	Optional<Payment> findByPurchaseId(Long purchaseId);

	boolean existsByPurchaseId(Long purchaseId);

	/**
	 * Payment 조회
	 * -> Payment.purchase로 이동
	 * -> Purchase.buyer로 이동
	 * -> buyer.id가 buyerId인 결제만 조회
	 * -> Payment.createdAt 기준 최신순 정렬
	 */
	@Query("""
	SELECT p
	FROM Payment p
	JOIN FETCH p.purchase pu
	JOIN FETCH pu.sale s
	JOIN FETCH s.product
	WHERE pu.buyer.id = :buyerId
	ORDER BY p.createdAt DESC
""")
	List<Payment> findAllByBuyerIdWithProduct(@Param("buyerId") Long buyerId);
}