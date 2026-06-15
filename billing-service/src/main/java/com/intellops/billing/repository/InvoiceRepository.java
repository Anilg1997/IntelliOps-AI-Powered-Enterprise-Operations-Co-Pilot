package com.intellops.billing.repository;

import com.intellops.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByOrderNumber(String orderNumber);
    List<Invoice> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Invoice> findByStatus(com.intellops.billing.entity.InvoiceStatus status);
}
