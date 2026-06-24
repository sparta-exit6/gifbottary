package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.User;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "giftion-sale")
@NoArgsConstructor
public class GifticonSale extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "seller_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User seller;

    @JoinColumn(name = "product_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private GifticonProduct product;

    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    private String encryptedPin;

    @Enumerated(EnumType.STRING)
    private PinValidationStatus pinCheckStatus;

    private Integer salePrice;

    private LocalDate expireAt;

    private Integer stock;

    public GifticonSale(User seller, GifticonProduct product, SaleType saleType, String encryptedPin, Integer salePrice, LocalDate expireAt, Integer stock) {
        this.seller = seller;
        this.product = product;
        this.saleType = saleType;
        this.encryptedPin = encryptedPin;
        this.salePrice = salePrice;
        this.expireAt = expireAt;
        this.stock = stock;
        this.saleStatus = SaleStatus.PENDING_REVIEW;
        this.pinCheckStatus = PinValidationStatus.PENDING;
    }

    public void validateSuccess() {
        this.pinCheckStatus = PinValidationStatus.VALID;
        this.saleStatus = SaleStatus.ON_SALE;
    }

    public void validateFail() {
        this.pinCheckStatus = PinValidationStatus.INVALID;
        this.saleStatus = SaleStatus.PIN_INVALID;
    }

    public void updateSaleInfo(Integer salePrice, LocalDate expireAt, Integer stock) {
        if (salePrice != null) {
            this.salePrice = salePrice;
        }
        if (expireAt != null) {
            this.expireAt = expireAt;
        }
        if (stock != null) {
            this.stock = stock;
        }
    }

    public void completeSale() {
        this.saleStatus = SaleStatus.SOLD_OUT;
    }

    public void cancelSale() {
        this.saleStatus = SaleStatus.CANCELLED;
    }
}
