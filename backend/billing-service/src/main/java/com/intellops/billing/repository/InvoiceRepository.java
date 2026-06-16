package com.intellops.billing.repository;

import com.intellops.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByOrderNumber(String orderNumber);

    List<Invoice> findByStatus(String status);

    List<Invoice> findByPaymentStatus(String paymentStatus);

    @Query("SELECT COUNT(i) FROM Invoice i")
    long countAll();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'PENDING'")
    long countPending();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'OVERDUE'")
    long countOverdue();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    long countPaid();
}
