package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매글에 포함되는 개별 기프티콘 핀 자산입니다.
 * 복호화가 필요한 핀번호 암호문과 중복 확인용 해시값을 분리해서 저장합니다.
 */
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

    @Column(name = "encrypted_pin", nullable = false)
    private String encryptedPin;

    @Column(name = "pin_hash", nullable = false, unique = true)
    private String pinHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_validation_status", nullable = false)
    private PinValidationStatus pinValidationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_sale_status", nullable = false)
    private PinSaleStatus pinSaleStatus;

    public GifticonPin(String encryptedPin, String pinHash) {
        this.encryptedPin = encryptedPin;
        this.pinHash = pinHash;
        this.pinValidationStatus = PinValidationStatus.PENDING;
        this.pinSaleStatus = PinSaleStatus.AVAILABLE;
    }

    public void assignSale(GifticonSale sale) {
        this.sale = sale;
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
