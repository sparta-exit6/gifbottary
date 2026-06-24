package com.example.gifbottary.domain.product.repositroy;

import com.example.gifbottary.domain.product.entity.GifticonProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonProductRepository extends JpaRepository<GifticonProduct, Long> {
    Optional<GifticonProduct> findByBrandAndProductName(String brand, String productName);
}