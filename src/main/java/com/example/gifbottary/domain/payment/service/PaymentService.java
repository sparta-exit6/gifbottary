package com.example.gifbottary.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.gifbottary.domain.payment.dto.request.PaymentCreateRequest;
import com.example.gifbottary.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.gifbottary.domain.payment.dto.response.PaymentCreateResponse;
import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.repository.PaymentRepository;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.repositroy.GifticonSaleRepository;
import com.example.gifbottary.domain.purchase.entity.Purchase;
import com.example.gifbottary.domain.purchase.repository.PurchaseRepository;
import com.example.gifbottary.domain.user.entity.User;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PurchaseRepository purchaseRepository;
	private final GifticonSaleRepository gifticonSaleRepository;
	private final EntityManager entityManager;

	/**
	 * 결제 생성
	 *
	 * @param request 결제 생성 요청 정보
	 * @return 생성된 결제 정보
	 */
	@Transactional
	public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
		GifticonSale sale = gifticonSaleRepository.findById(request.saleId())
			.orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));

		User buyer = entityManager.getReference(User.class, request.buyerId());

		Purchase purchase = Purchase.create(buyer, sale, request.quantity());
		Purchase savedPurchase = purchaseRepository.save(purchase);

		Payment payment = Payment.create(savedPurchase);
		Payment savedPayment = paymentRepository.save(payment);

		return PaymentCreateResponse.from(savedPayment);
	}

	/**
	 * 결제 확정
	 *
	 * @param request 결제 확정 요청 정보
	 * @return 확정된 결제 정보
	 */
	@Transactional
	public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
		Payment payment = paymentRepository.findByPortOnePaymentId(request.portOnePaymentId())
			.orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

		Purchase purchase = payment.getPurchase();

		payment.complete();

		if (purchase.getSale().getSaleType() == SaleType.PERSONAL) {
			purchase.confirmPersonalPurchase();
		} else {
			purchase.completePayment();
		}

		purchase.getSale().deductStock();

		return PaymentConfirmResponse.from(payment);
	}
}
