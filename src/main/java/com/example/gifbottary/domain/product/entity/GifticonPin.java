package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "gifticon_pin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GifticonPin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "sale_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private GifticonSale sale;

    @Column(name = "encrypted_pin", nullable = false, unique = true)
    private String encryptedPin;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_validation_status", nullable = false)
    private PinValidationStatus pinValidationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_sale_status", nullable = false)
    private PinSaleStatus pinSaleStatus;

    public GifticonPin(String encryptedPin) {
        this.encryptedPin = encryptedPin;
        this.pinValidationStatus = PinValidationStatus.PENDING;
        this.pinSaleStatus = PinSaleStatus.AVAILABLE;
    }

    public void assignSale(GifticonSale sale) {
        this.sale = sale;

    }

    public void removeSale() {
        this.sale = null;
    }

    public void validatePin() {
        this.pinValidationStatus = PinValidationStatus.VALID;
    }

    public void invalidatePin() {
        this.pinValidationStatus = PinValidationStatus.INVALID;
    }

    public void markSold() {
        this.pinSaleStatus = PinSaleStatus.SOLD;
    }

    public boolean isAvailable() {
        return this.pinValidationStatus == PinValidationStatus.VALID
                && this.pinSaleStatus == PinSaleStatus.AVAILABLE;
    }
}