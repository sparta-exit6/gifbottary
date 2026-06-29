package com.example.gifbottary.domain.payment.dto.response;

import java.time.LocalDateTime;

import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;

public record PaymentGetListResponse(
	Long paymentId,    				//결제 ID
	Long purchaseId,				//구매 ID
	Long saleId,					//판매글 ID
	String productName,				//상품명
	String brand,					//브랜드명
	int amount,						//결제 금액
	int quantity,					//구매 수량
	PaymentStatus paymentStatus,	//결제 상태
	PurchaseStatus purchaseStatus,	//구매 상태
	LocalDateTime paidAt,			//결제 완료 시간
	LocalDateTime createdAt			//결제 생성 시간
) {
	public static PaymentGetListResponse from(Payment payment) {
		return new PaymentGetListResponse(
			payment.getId(),
			payment.getPurchase().getId(),
			payment.getPurchase().getSale().getId(),
			payment.getPurchase().getSale().getProduct().getProductName(),
			payment.getPurchase().getSale().getProduct().getBrand(),
			payment.getAmount(),
			payment.getPurchase().getQuantity(),
			payment.getStatus(),
			payment.getPurchase().getPurchaseStatus(),
			payment.getPaidAt(),
			payment.getCreatedAt()
		);
	}
}