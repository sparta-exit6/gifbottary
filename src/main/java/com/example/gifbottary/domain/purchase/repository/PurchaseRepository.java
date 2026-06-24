package com.example.gifbottary.domain.purchase.repository;

import com.example.gifbottary.domain.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
