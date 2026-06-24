package com.example.gifbottary.domain.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.order.entity.Order;

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
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	@Column(name = "portone_payment_id", nullable = false, unique = true)
	private String portOnePaymentId;

	//실제 결제 금액 (Order.totalPrice와 같은 금액)
	@Column(nullable = false, columnDefinition = "int UNSIGNED")
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PaymentStatus status = PaymentStatus.IN_PROGRESS;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	public Payment(Order order) {
		this.order = order;
		this.amount = order.getTotalPrice();
		this.portOnePaymentId = generatePortOnePaymentId();
	}

	public static Payment create(Order order) {
		return new Payment(order);
	}

	public void complete() {
		changeStatus(PaymentStatus.PAID);
		this.paidAt = LocalDateTime.now();
	}

	public void fail() {
		changeStatus(PaymentStatus.FAILED);
	}

	public void cancel() {
		changeStatus(PaymentStatus.CANCELLED);
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
