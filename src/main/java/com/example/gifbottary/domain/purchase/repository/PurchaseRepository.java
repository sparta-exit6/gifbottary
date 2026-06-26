package com.example.gifbottary.domain.purchase.repository;

import com.example.gifbottary.domain.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

	@EntityGraph(attributePaths = {"buyer", "sale", "sale.product", "sale.pins"})
	Optional<Purchase> findDetailById(Long id);

	@EntityGraph(attributePaths = {"buyer", "sale", "sale.product", "sale.pins"})
	Page<Purchase> findAllByBuyer_IdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
}