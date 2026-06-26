package com.example.gifbottary.domain.payment.dto.response;

import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;

public record PaymentConfirmResponse(
	Long paymentId,
	Long purchaseId,
	String portOnePaymentId,
	int amount,
	PaymentStatus paymentStatus,
	PurchaseStatus purchaseStatus
) {
	public static PaymentConfirmResponse from(Payment payment) {
		return new PaymentConfirmResponse(
			payment.getId(),
			payment.getPurchase().getId(),
			payment.getPortOnePaymentId(),
			payment.getAmount(),
			payment.getStatus(),
			payment.getPurchase().getPurchaseStatus()
		);
	}
}
