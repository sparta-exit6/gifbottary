package com.example.gifbottary.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmRequest(
	@NotBlank(message = "포트원 결제 ID는 필수입니다.")
	String portOnePaymentId
) {
}
