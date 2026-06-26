package com.example.gifbottary.domain.purchase.repository;

import java.util.List;

import com.example.gifbottary.domain.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
	List<Purchase> findAllByBuyerId(Long buyerId);
}
