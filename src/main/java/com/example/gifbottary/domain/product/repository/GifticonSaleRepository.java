package com.example.gifbottary.domain.product.repository;

import com.example.gifbottary.domain.product.entity.GifticonSale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonSaleRepository extends JpaRepository<GifticonSale, Long>, ProductSearchRepository {

    @EntityGraph(attributePaths = {"seller", "product", "pins"})
    Optional<GifticonSale> findDetailById(Long id);
}
