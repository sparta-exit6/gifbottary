package com.example.gifbottary.domain.product.repositroy;

import com.example.gifbottary.domain.product.entity.GifticonSale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GifticonSaleRepository extends JpaRepository<GifticonSale, Long>, JpaSpecificationExecutor<GifticonSale> {

    @EntityGraph(attributePaths = {"seller", "product", "pins"})
    Optional<GifticonSale> findDetailById(Long id);
}
