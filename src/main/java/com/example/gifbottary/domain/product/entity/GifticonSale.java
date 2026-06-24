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
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "gifticon_sale")
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

    private Integer salePrice;

    private LocalDate expireAt;

    private Integer stock;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GifticonPin> pins = new ArrayList<>();

    public GifticonSale(User seller, GifticonProduct product, SaleType saleType, Integer salePrice, LocalDate expireAt, Integer stock) {
        validateStockBySaleType(saleType, stock);
        this.seller = seller;
        this.product = product;
        this.saleType = saleType;
        this.salePrice = salePrice;
        this.expireAt = expireAt;
        this.stock = stock;
        this.saleStatus = SaleStatus.PENDING_REVIEW;
    }

    public void updateSaleInfo(Integer salePrice, LocalDate expireAt, Integer stock) {
        if (salePrice != null) {
            this.salePrice = salePrice;
        }
        if (expireAt != null) {
            this.expireAt = expireAt;
        }
        if (stock != null) {
            validateStockBySaleType(this.saleType, stock);
            this.stock = stock;
        }
    }

    public void addPin(GifticonPin pin) {
        this.pins.add(pin);
        pin.assignSale(this);
    }

    public void removePin(GifticonPin pin) {
        this.pins.remove(pin);
        pin.removeSale();
    }

    public void increaseStock() {
        this.stock += 1;
    }

    public void deductStock() {
        if (this.stock == null || this.stock < 1) {
            throw new IllegalStateException("차감할 재고가 없습니다.");
        }

        this.stock -= 1;

        if (this.stock == 0) {
            completeSale();
        }
    }

    public void completeSale() {
        this.saleStatus = SaleStatus.SOLD_OUT;
    }

    public void cancelSale() {
        this.saleStatus = SaleStatus.CANCELLED;
    }

    public void updateSaleStatusByStock() {
        if (this.stock != null && this.stock > 0) {
            this.saleStatus = SaleStatus.ON_SALE;
            return;
        }

        this.saleStatus = SaleStatus.SOLD_OUT;
    }

    private void validateStockBySaleType(SaleType saleType, Integer stock) {
        if (stock == null || stock < 1) {
            throw new IllegalArgumentException("stock은 1 이상이어야 합니다.");
        }

        if (saleType == SaleType.PERSONAL && stock != 1) {
            throw new IllegalArgumentException("개인 판매 상품의 stock은 반드시 1이어야 합니다.");
        }
    }
}
