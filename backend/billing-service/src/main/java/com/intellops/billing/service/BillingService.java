package com.intellops.billing.service;

import com.intellops.billing.entity.Invoice;
import com.intellops.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(String orderNumber, String customerName, String customerEmail,
                                  BigDecimal totalAmount, BigDecimal taxAmount) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .orderNumber(orderNumber)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .totalAmount(totalAmount)
                .taxAmount(taxAmount)
                .status("ISSUED")
                .paymentStatus("PENDING")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice created: {} for order: {}", invoice.getInvoiceNumber(), orderNumber);
        return invoice;
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoice(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoiceByOrder(String orderNumber) {
        return invoiceRepository.findByOrderNumber(orderNumber);
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoices(String status) {
        if (status != null && !status.isEmpty()) {
            return invoiceRepository.findByStatus(status);
        }
        return invoiceRepository.findAll();
    }

    @Transactional
    public Invoice processPayment(String invoiceNumber, String paymentMethod, String transactionId) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        invoice.setPaymentStatus("PAID");
        invoice.setPaymentMethod(paymentMethod);
        invoice.setTransactionId(transactionId);
        invoice.setPaidDate(LocalDate.now());
        invoice.setStatus("PAID");

        invoice = invoiceRepository.save(invoice);
        log.info("Payment processed for invoice: {}", invoiceNumber);
        return invoice;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBillingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInvoices", invoiceRepository.countAll());
        stats.put("pendingInvoices", invoiceRepository.countPending());
        stats.put("overdueInvoices", invoiceRepository.countOverdue());
        stats.put("paidInvoices", invoiceRepository.countPaid());
        return stats;
    }

    @KafkaListener(topics = "order-events", groupId = "billing-service")
    public void handleOrderEvent(String event) {
        log.info("Received order event: {}", event);
        // Parse event and create invoice if needed
        // In production, this would use proper JSON parsing
    }

    private String generateInvoiceNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "INV-" + date + "-" + random;
    }
}
