package com.example.gifbottary.domain.purchase.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.User;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.purchase.enums.PinStatus;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime purchasedAt;

    private LocalDateTime confirmedAt;

    public static Purchase createPlatformPurchase(User buyer, GifticonSale sale) {
        Purchase purchase = new Purchase();
        purchase.buyer = buyer;
        purchase.sale = sale;
        purchase.purchaseStatus = PurchaseStatus.PAID;
        purchase.pinStatus = PinStatus.MASKED;
        purchase.refundLocked = false;
        purchase.purchasedAt = LocalDateTime.now();
        return purchase;
    }

    public static Purchase createPersonalPurchase(User buyer, GifticonSale sale) {
        Purchase purchase = new Purchase();
        purchase.buyer = buyer;
        purchase.sale = sale;
        purchase.purchaseStatus = PurchaseStatus.CONFIRMED;
        purchase.pinStatus = PinStatus.REVEALED;
        purchase.refundLocked = true;
        purchase.purchasedAt = LocalDateTime.now();
        purchase.confirmedAt = LocalDateTime.now();
        return purchase;
    }

    public void revealPin() {
        this.pinStatus = PinStatus.REVEALED;
        this.refundLocked = true;
        this.purchaseStatus = PurchaseStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public boolean isConfirmed() {
        return this.purchaseStatus == PurchaseStatus.CONFIRMED;
    }

    public boolean isMasked() {
        return this.pinStatus == PinStatus.MASKED;
    }
}
