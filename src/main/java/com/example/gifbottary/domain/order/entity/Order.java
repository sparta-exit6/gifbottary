package com.example.gifbottary.domain.order.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//주문한 유저 아이디
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	//주문한 상푸 아이디
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sale_id", nullable = false)
	private GifticonSale sale;

	//주문 타입 (중고 or 플랫폼)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SaleType orderType;

	//주문 수량
	@Column(nullable = false)
	private int quantity;

	//개당 가격
	@Column(name = "orderPrice", nullable = false)
	private int orderPrice;

	//총 합계
	@Column(name = "total_price", nullable = false)
	private int totalPrice;

	//주문 상태
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.PENDING_PAYMENT;

	private Order(User buyer, GifticonSale sale, int quantity) {
		validateQuantity(sale, quantity);

		this.buyer = buyer;
		this.sale = sale;
		this.orderType = sale.getSaleType();
		this.quantity = quantity;
		this.orderPrice = sale.getSalePrice();
		this.totalPrice = sale.getSalePrice() * quantity;
		this.status = OrderStatus.PENDING_PAYMENT;
	}

	public static Order create(User buyer, GifticonSale sale, int quantity) {
		return new Order(buyer, sale, quantity);
	}

	public void confirm() {
		changeStatus(OrderStatus.CONFIRMED);
	}

	public void cancel() {
		changeStatus(OrderStatus.CANCELLED);
	}

	public boolean isPlatformOrder() {
		return this.orderType == SaleType.PLATFORM;
	}

	public boolean isPersonalOrder() {
		return this.orderType == SaleType.PERSONAL;
	}

	private void changeStatus(OrderStatus target) {
		if (!this.status.canTransitTo(target)) {
			throw new IllegalStateException("주문 상태를 변경할 수 없습니다.");
		}
		this.status = target;
	}

	private void validateQuantity(GifticonSale sale, int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("주문 수량은 1개 이상이여야 합니다.");
		}

		if (sale.getSaleType() == SaleType.PERSONAL && quantity != 1) {
			throw new IllegalArgumentException("개인 판매 상품은 1개만 주문할 수 있습니다.");
		}

		if (sale.getStock() < quantity) {
			throw new IllegalArgumentException("재고가 부족합니다.");

		}
	}
}
