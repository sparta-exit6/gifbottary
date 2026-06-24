package com.example.gifbottary.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gifbottary.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPortOnePaymentId(String portOnePaymentId);

	Optional<Payment> findByPurchaseId(Long purchaseId);

	boolean existsByPurchaseId(Long purchaseId);
}