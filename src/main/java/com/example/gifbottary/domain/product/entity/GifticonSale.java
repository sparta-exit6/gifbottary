package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 판매글 엔티티입니다.
 * 실제 핀 자산은 GifticonPin이 관리하고, 판매글은 가격/상태/재고를 관리합니다.
 */
@Getter
@Entity
@Table(name = "gifticon_sale")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(nullable = false)
    private SaleType saleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus saleStatus;

    @Column(nullable = false)
    private Integer salePrice;

    @Column(nullable = false)
    private LocalDate expireAt;

    @Column(nullable = false)
    private Integer stock;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GifticonPin> pins = new ArrayList<>();

    public GifticonSale(User seller, GifticonProduct product, SaleType saleType, Integer salePrice, LocalDate expireAt) {
        this.seller = seller;
        this.product = product;
        this.saleType = saleType;
        this.salePrice = salePrice;
        this.expireAt = expireAt;
        this.stock = 0;
        this.saleStatus = SaleStatus.PENDING_REVIEW;
    }

    /**
     * 판매글의 수정 가능한 정보만 갱신합니다.
     */
    public void updateSaleInfo(Integer salePrice) {
        if (salePrice != null) {
            this.salePrice = salePrice;
        }
    }

    /**
     * 판매글에 핀을 연결합니다.
     */
    public void addPin(GifticonPin pin) {
        this.pins.add(pin);
        pin.assignSale(this);
        validatePinCountBySaleType();
    }

    /**
     * 핀 상태를 기준으로 재고와 판매 상태를 다시 계산합니다.
     */
    public void synchronizeStockAndStatus() {
        if (this.saleStatus == SaleStatus.CANCELLED) {
            return;
        }

        int availableCount = (int) pins.stream()
                .filter(GifticonPin::isAvailable)
                .count();
        int pendingCount = (int) pins.stream()
                .filter(pin -> pin.getPinValidationStatus() == PinValidationStatus.PENDING)
                .count();
        int soldCount = (int) pins.stream()
                .filter(pin -> pin.getPinSaleStatus() == PinSaleStatus.SOLD)
                .count();

        this.stock = availableCount;

        if (availableCount > 0) {
            this.saleStatus = SaleStatus.ON_SALE;
            return;
        }

        if (pendingCount > 0) {
            this.saleStatus = SaleStatus.PENDING_REVIEW;
            return;
        }

        if (soldCount > 0) {
            this.saleStatus = SaleStatus.SOLD_OUT;
            return;
        }

        this.saleStatus = SaleStatus.PIN_INVALID;
    }

    /**
     * 판매자가 직접 변경 가능한 상태만 허용합니다.
     */
    public void changeSaleStatus(SaleStatus saleStatus) {
        if (saleStatus == SaleStatus.CANCELLED) {
            this.saleStatus = SaleStatus.CANCELLED;
            return;
        }

        if (saleStatus == SaleStatus.PENDING_REVIEW) {
            this.saleStatus = SaleStatus.PENDING_REVIEW;
            return;
        }

        if (saleStatus == SaleStatus.ON_SALE && this.stock > 0) {
            this.saleStatus = SaleStatus.ON_SALE;
            return;
        }

        throw new IllegalStateException("현재 상태에서는 요청한 판매 상태로 변경할 수 없습니다.");
    }

    public boolean isOwnedBy(Long sellerId) {
        return this.seller.getId().equals(sellerId);
    }

    /**
     * 현재 판매글에서 실제 판매 가능한 핀 수를 반환합니다.
     */
    public int countAvailablePins() {
        return (int) this.pins.stream()
                .filter(GifticonPin::isAvailable)
                .count();
    }

    private void validatePinCountBySaleType() {
        if (this.saleType == SaleType.PERSONAL && this.pins.size() > 1) {
            throw new IllegalStateException("개인 판매글은 핀을 1개만 가질 수 있습니다.");
        }
    }

    /**
     * 재고 차감
     * !! 지금 Purchase에서 어떤 GifticonPin 샀는지 저장하지 않고있음.
     * @param quantity 판매할 수량
     */
    public void sellPins(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("판매 수량은 1개 이상이어야 합니다.");
        }

        List<GifticonPin> availablePins = this.pins.stream()
            .filter(GifticonPin::isAvailable)
            .limit(quantity)
            .toList();

        if (availablePins.size() < quantity) {
            throw new IllegalStateException("판매 가능한 핀이 부족합니다.");
        }

        availablePins.forEach(GifticonPin::markSold);
        synchronizeStockAndStatus();
    }
}