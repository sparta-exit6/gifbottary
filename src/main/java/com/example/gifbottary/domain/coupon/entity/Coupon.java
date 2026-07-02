package com.example.gifbottary.domain.coupon.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int discountPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    public Coupon(String name, int discountPrice, int totalQuantity) {
        this.name = name;
        this.discountPrice = discountPrice;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
    }

    public void issue() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw new ServiceException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
        this.issuedQuantity++;
    }
}
