package com.example.gifbottary.domain.payment.dto.response;

import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;

public record PaymentCreateResponse(
	Long paymentId,
	Long purchaseId,
	String portOnePaymentId,
	int amount,
	PaymentStatus status
) {
	public static PaymentCreateResponse from(Payment payment) {
		return new PaymentCreateResponse(
			payment.getId(),
			payment.getPurchase().getId(),
			payment.getPortOnePaymentId(),
			payment.getAmount(),
			payment.getStatus()
		);
	}
}
