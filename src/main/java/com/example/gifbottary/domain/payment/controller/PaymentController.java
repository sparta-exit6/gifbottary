package com.example.gifbottary.domain.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.gifbottary.common.response.CommonResponse;
import com.example.gifbottary.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.gifbottary.domain.payment.dto.request.PaymentCreateRequest;
import com.example.gifbottary.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.gifbottary.domain.payment.dto.response.PaymentCreateResponse;
import com.example.gifbottary.domain.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CommonResponse<PaymentCreateResponse> createPayment(
		@Valid @RequestBody PaymentCreateRequest request
	) {
		return CommonResponse.success("결제가 생성되었습니다.", paymentService.createPayment(request));
	}

	@PostMapping("/confirm")
	public CommonResponse<PaymentConfirmResponse> confirmPayment(
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		return CommonResponse.success("결제가 확정되었습니다.", paymentService.confirmPayment(request));
	}
}
