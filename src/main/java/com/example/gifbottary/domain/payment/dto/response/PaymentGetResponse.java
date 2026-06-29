package com.example.gifbottary.domain.payment.dto.response;

import java.time.LocalDateTime;

import com.example.gifbottary.domain.payment.entity.Payment;
import com.example.gifbottary.domain.payment.entity.PaymentStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.purchase.enums.PinStatus;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;

public record PaymentGetResponse(
	Long paymentId,
	Long purchaseId,
	Long saleId,

	String portOnePaymentId, 		//포트원 결제 ID

	String brand,
	String productName,
	String imageUrl,

	SaleType saleType,				//플랫폼 / 중고 구분
	int quantity,
	int unitPrice,					//구매 당시 단가
	int amount,						//총 결제 금액

	PaymentStatus paymentStatus,	//결제 상태
	PurchaseStatus purchaseStatus,	//구매 상태
	PinStatus pinStatus,			//핀 공개 여부
	Boolean refundLocked,			//환불 가능 여부

	LocalDateTime paidAt,			//결제 완료 시간
	LocalDateTime cancelledAt,		//환불 / 취소 시간
	LocalDateTime purchasedAt,
	LocalDateTime confirmedAt,
	LocalDateTime createdAt
) {
	public static PaymentGetResponse from(Payment payment) {
		return new PaymentGetResponse(
			payment.getId(),
			payment.getPurchase().getId(),
			payment.getPurchase().getSale().getId(),

			payment.getPortOnePaymentId(),

			payment.getPurchase().getSale().getProduct().getBrand(),
			payment.getPurchase().getSale().getProduct().getProductName(),
			payment.getPurchase().getSale().getProduct().getImageUrl(),

			payment.getPurchase().getSale().getSaleType(),
			payment.getPurchase().getQuantity(),
			payment.getPurchase().getUnitPrice(),
			payment.getAmount(),

			payment.getStatus(),
			payment.getPurchase().getPurchaseStatus(),
			payment.getPurchase().getPinStatus(),
			payment.getPurchase().getRefundLocked(),

			payment.getPaidAt(),
			payment.getCancelledAt(),
			payment.getPurchase().getPurchasedAt(),
			payment.getPurchase().getConfirmedAt(),
			payment.getCreatedAt()
		);
	}
}