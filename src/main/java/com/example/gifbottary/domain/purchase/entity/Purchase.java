package com.example.gifbottary.domain.purchase.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.purchase.enums.PinStatus;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구매 엔티티입니다.
 *
 * 개인 상품과 플랫폼 상품 모두 이 엔티티로 관리하며,
 * 구매 생성 -> 결제 대기 -> 결제 완료 -> 구매 확정(핀 노출) 흐름을 상태 전이로 표현합니다.
 */
@Getter
@Entity
@Table(name = "purchase")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "buyer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User buyer;

    @JoinColumn(name = "sale_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private GifticonSale sale;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus purchaseStatus;

    @Enumerated(EnumType.STRING)
    private PinStatus pinStatus;

    private Boolean refundLocked;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int totalPrice;

    private LocalDateTime purchasedAt;

    private LocalDateTime confirmedAt;


    /**
     * 구매를 최초 생성합니다.
     *
     * 생성 시점은 아직 결제가 끝나지 않은 상태이므로
     * 기본값은 결제 대기 + 마스킹 + 환불 가능 상태입니다.
     */
    public static Purchase create(User buyer, GifticonSale sale, int quantity) {
        validateQuantity(quantity);

        Purchase purchase = new Purchase();

        purchase.buyer = buyer;
        purchase.sale = sale;

        purchase.quantity = quantity;
        purchase.unitPrice = sale.getSalePrice();
        purchase.totalPrice = quantity * sale.getSalePrice();

        purchase.purchaseStatus = PurchaseStatus.PENDING_PAYMENT;
        purchase.pinStatus = PinStatus.MASKED;
        purchase.refundLocked = false;

        return purchase;
    }

    /**
     * 결제 완료 처리입니다.
     *
     * 결제 대기 상태에서만 결제 완료로 전이할 수 있습니다.
     */
    public void markPaid() {
        if (this.purchaseStatus != PurchaseStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태에서만 결제 완료 처리할 수 있습니다.");
        }

        this.purchaseStatus = PurchaseStatus.PAID;
        this.purchasedAt = LocalDateTime.now();
    }

    /**
     * 플랫폼 상품 구매 확정 처리입니다.
     *
     * 마스킹된 핀을 노출하는 시점에 호출되며,
     * 이 시점부터 환불이 잠기고 구매 확정 상태가 됩니다.
     */
    public void confirmPlatformPurchase() {
        if (this.purchaseStatus != PurchaseStatus.PAID) {
            throw new IllegalStateException("결제 완료 상태에서만 구매 확정할 수 있습니다.");
        }

        this.purchaseStatus = PurchaseStatus.CONFIRMED;
        this.pinStatus = PinStatus.REVEALED;
        this.refundLocked = true;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 개인 상품 구매 확정 처리입니다.
     *
     * 개인 상품은 구매 즉시 핀을 노출하므로,
     * 결제 완료와 구매 확정을 사실상 함께 처리하는 용도로 사용합니다.
     */
    public void confirmPersonalPurchase() {
        if (this.purchaseStatus != PurchaseStatus.PAID) {
        throw new IllegalStateException("결제 완료 상태에서만 개인 상품 구매 확정이 가능합니다.");
        }
        
        if (this.purchasedAt == null) {
            this.purchasedAt = LocalDateTime.now();
        }

        this.purchaseStatus = PurchaseStatus.CONFIRMED;
        this.pinStatus = PinStatus.REVEALED;
        this.refundLocked = true;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 환불 처리입니다.
     *
     * 핀 노출 이후에는 환불 불가 정책이므로 refundLocked가 true면 예외를 던집니다.
     */
    public void cancel() {
        if (Boolean.TRUE.equals(this.refundLocked)) {
            throw new IllegalStateException("환불이 불가능한 구매입니다.");
        }

        this.purchaseStatus = PurchaseStatus.REFUNDED;
    }

    /**
     * 구매자 본인 여부를 검증합니다.
     */
    public boolean isOwner(Long userId) {
        return buyer.getId().equals(userId);
    }

    public boolean isConfirmed() {
        return this.purchaseStatus == PurchaseStatus.CONFIRMED;
    }

    /**
     * 핀이 아직 마스킹 상태인지 확인합니다.
     */
    public boolean isMasked() {
        return this.pinStatus == PinStatus.MASKED;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }
}
