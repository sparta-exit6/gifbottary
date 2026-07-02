package com.example.gifbottary.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
	String id,
	String paymentId,
	String status,
	Amount amount
) {

	private static final String PAID_STATUS = "PAID";

	public boolean isPaid() {
		return PAID_STATUS.equals(status);
	}

	public int totalAmount() {
		if (amount == null || amount.total() == null) {
			return 0;
		}

		return amount.total();
	}

	public String resolvedPaymentId() {
		if (paymentId != null && !paymentId.isBlank()) {
			return paymentId;
		}

		return id;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Amount(
		Integer total
	) {
	}
}
