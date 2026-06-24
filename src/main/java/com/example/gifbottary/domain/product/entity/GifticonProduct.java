package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "gifticon_product", uniqueConstraints = {
        @UniqueConstraint(name = "uk_gifticon_product_brand_name", columnNames = {"brand", "product_name"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GifticonProduct  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;
    
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer faceValue;
    private String imageUrl;

    public GifticonProduct(String brand, String productName, Integer faceValue, String imageUrl) {
        this.brand = brand;
        this.productName = productName;
        this.faceValue = faceValue;
        this.imageUrl = imageUrl;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
