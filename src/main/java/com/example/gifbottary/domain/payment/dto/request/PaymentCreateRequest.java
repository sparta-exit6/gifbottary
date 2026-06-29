package com.example.gifbottary.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
	@NotNull(message = "판매 ID는 필수입니다.")
	Long saleId,

	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	int quantity
) {
}