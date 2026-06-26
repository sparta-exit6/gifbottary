package com.example.gifbottary.domain.payment.entity;

/**
 * READY : 결제 생성 완료, PortOne 결제 전
 * COMPLETED : PortOne 결제 성공
 * FAILED : PortOne 결제 실패
 * REFUNDED : 환불 완료
 */
public enum PaymentStatus {

	READY {
		@Override
		public boolean canTransitTo(PaymentStatus target) {
			return target == COMPLETED || target == FAILED;
		}
	},

	COMPLETED {
		@Override
		public boolean canTransitTo(PaymentStatus target) {
			return target == REFUNDED;
		}
	},

	FAILED {
		@Override
		public boolean canTransitTo(PaymentStatus target) {
			return false;
		}
	},

	REFUNDED {
		@Override
		public boolean canTransitTo(PaymentStatus target) {
			return false;
		}
	};

	public abstract boolean canTransitTo(PaymentStatus target);
}