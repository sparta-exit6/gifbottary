package com.example.gifbottary.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPortOnePaymentId(String portOnePaymentId);

	Optional<Payment> findByPurchaseId(Long purchaseId);

	boolean existsByPurchaseId(Long purchaseId);

	@Query("""
	SELECT COALESCE(SUM(pu.quantity), 0)
	FROM Payment p
	JOIN p.purchase pu
	WHERE pu.sale.id = :saleId
	  AND p.status = :status
	""")
	long sumQuantityBySaleIdAndStatus(
		@Param("saleId") Long saleId,
		@Param("status") PaymentStatus status
	);

	List<Payment> findAllByStatusAndCreatedAtBefore(
		PaymentStatus status,
		LocalDateTime createdAt
	);

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

	/**
	 * 결제 상세 조회
	 */
	@Query("""
	SELECT p
	FROM Payment p
	JOIN FETCH p.purchase pu
	JOIN FETCH pu.sale s
	JOIN FETCH s.product
	WHERE p.id = :paymentId
	""")
	Optional<Payment> findByIdWithProduct(@Param("paymentId") Long paymentId);
}
