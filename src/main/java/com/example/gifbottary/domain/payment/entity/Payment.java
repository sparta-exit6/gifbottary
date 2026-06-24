package com.example.gifbottary.domain.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.purchase.entity.Purchase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_id", nullable = false, unique = true)
	private Purchase purchase;

	@Column(name = "portone_payment_id", nullable = false, unique = true)
	private String portOnePaymentId;

	@Column(nullable = false)
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	public static Payment create(Purchase purchase) {
		Payment payment = new Payment();

		payment.purchase = purchase;
		payment.amount = purchase.getTotalPrice();
		payment.portOnePaymentId = generatePortOnePaymentId();
		payment.status = PaymentStatus.READY;

		return payment;
	}

	public void complete() {
		changeStatus(PaymentStatus.COMPLETED);
		this.paidAt = LocalDateTime.now();
	}

	public void fail() {
		changeStatus(PaymentStatus.FAILED);
	}

	public void refund() {
		changeStatus(PaymentStatus.REFUNDED);
		this.cancelledAt = LocalDateTime.now();
	}

	private void changeStatus(PaymentStatus target) {
		if (!this.status.canTransitTo(target)) {
			throw new IllegalStateException("결제 상태를 변경할 수 없습니다.");
		}

		this.status = target;
	}

	private static String generatePortOnePaymentId() {
		return "pay_" + UUID.randomUUID();
	}
}
