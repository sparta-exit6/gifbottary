package com.example.gifbottary.domain.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.order.entity.Order;
import com.example.gifbottary.domain.order.repository.OrderRepository;
import com.example.gifbottary.domain.payment.dto.request.PaymentCreateRequest;
import com.example.gifbottary.domain.payment.dto.response.PaymentCreateResponse;
import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	@Transactional
	public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
		Order order = orderRepository.findById(request.orderId())
			.orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

		paymentRepository.findByOrder(order)
			.ifPresent(payment -> {
				throw new ServiceException(HttpStatus.CONFLICT, "이미 생성된 결제가 있습니다.");
			});

		Payment payment = Payment.create(order);
		Payment savedPayment = paymentRepository.save(payment);

		return PaymentCreateResponse.from(savedPayment);
	}
}
