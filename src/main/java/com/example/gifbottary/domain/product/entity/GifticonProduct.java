package com.example.gifbottary.domain.product.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "gifticon_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GifticonProduct  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String productName;
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
