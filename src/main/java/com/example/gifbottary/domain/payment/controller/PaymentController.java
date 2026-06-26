package com.example.gifbottary.domain.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.gifbottary.common.response.ApiResponse;
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
	public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPayment(
		@Valid @RequestBody PaymentCreateRequest request
	) {
		PaymentCreateResponse response = paymentService.createPayment(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}

	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		PaymentConfirmResponse response = paymentService.confirmPayment(request);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
