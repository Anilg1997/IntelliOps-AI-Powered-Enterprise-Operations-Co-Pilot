package com.intellops.billing.repository;

import com.intellops.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    List<Payment> findByInvoiceNumber(String invoiceNumber);
    List<Payment> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
