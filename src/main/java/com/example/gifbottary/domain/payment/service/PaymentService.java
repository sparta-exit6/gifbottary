package com.example.gifbottary.domain.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.gifbottary.domain.payment.dto.request.PaymentCreateRequest;
import com.example.gifbottary.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.gifbottary.domain.payment.dto.response.PaymentCreateResponse;
import com.example.gifbottary.domain.payment.dto.response.PaymentGetListResponse;
import com.example.gifbottary.domain.payment.dto.response.PaymentGetResponse;
import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.repository.PaymentRepository;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.purchase.entity.Purchase;
import com.example.gifbottary.domain.purchase.repository.PurchaseRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.infra.portone.PortOneClient;
import com.example.gifbottary.infra.portone.dto.PortOnePaymentResponse;

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
	private final PortOneClient portOneClient;

	/**
	 * 결제 생성
	 *
	 * @param request 결제 생성 요청 정보
	 * @return 생성된 결제 정보
	 */
	@Transactional
	public PaymentCreateResponse createPayment(Long buyerId, PaymentCreateRequest request) {
		User buyer = entityManager.getReference(User.class, buyerId);

		GifticonSale sale = gifticonSaleRepository.findById(request.saleId())
			.orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));

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

		if (payment.getStatus() != PaymentStatus.READY) {
			throw new ServiceException(ErrorCode.CONFLICT);
		}

		Purchase purchase = payment.getPurchase();

		PortOnePaymentResponse portOnePayment = portOneClient.getPayment(request.portOnePaymentId());
		validatePortOnePayment(payment, portOnePayment);

		payment.complete();
		purchase.markPaid();

		if (purchase.getSale().getSaleType() == SaleType.PERSONAL) {
			purchase.confirmPersonalPurchase();
		}

		// 결제 완료시 재고 차감
		purchase.getSale().sellPins(purchase.getQuantity());

		return PaymentConfirmResponse.from(payment);
	}

	private void validatePortOnePayment(Payment payment, PortOnePaymentResponse portOnePayment) {
		if (!portOnePayment.isPaid()) {
			throw new ServiceException(ErrorCode.PAYMENT_NOT_PAID_AT_PG);
		}

		if (portOnePayment.totalAmount() != payment.getAmount()) {
			throw new ServiceException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
	}

	/**
	 * 결제 목록 조회
	 */
	public List<PaymentGetListResponse> getListPayment(Long buyerId) {
		return paymentRepository.findAllByBuyerIdWithProduct(buyerId)
			.stream()
			.map(PaymentGetListResponse::from)
			.toList();
	}

	/**
	 * 결제 상세 조회
	 */
	public PaymentGetResponse getPayment(Long paymentId, Long buyerId) {
		Payment payment = paymentRepository.findByIdWithProduct(paymentId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND));

		if (!payment.getPurchase().isOwner(buyerId)) {
			throw new ServiceException(ErrorCode.PAYMENT_OWNERSHIP_MISMATCH);
		}

		return PaymentGetResponse.from(payment);
	}




}
