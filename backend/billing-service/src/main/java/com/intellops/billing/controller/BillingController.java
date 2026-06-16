package com.intellops.billing.controller;

import com.intellops.billing.entity.Invoice;
import com.intellops.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Map<String, Object> request) {
        Invoice invoice = billingService.createInvoice(
                (String) request.get("orderNumber"),
                (String) request.get("customerName"),
                (String) request.get("customerEmail"),
                new BigDecimal(request.get("totalAmount").toString()),
                new BigDecimal(request.getOrDefault("taxAmount", "0").toString())
        );
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable String invoiceNumber) {
        return billingService.getInvoice(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices/order/{orderNumber}")
    public ResponseEntity<Invoice> getInvoiceByOrder(@PathVariable String orderNumber) {
        return billingService.getInvoiceByOrder(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> listInvoices(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(billingService.listInvoices(status));
    }

    @PostMapping("/invoices/{invoiceNumber}/pay")
    public ResponseEntity<Invoice> processPayment(
            @PathVariable String invoiceNumber,
            @RequestBody Map<String, String> request) {
        Invoice invoice = billingService.processPayment(
                invoiceNumber,
                request.get("paymentMethod"),
                request.get("transactionId")
        );
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBillingStats() {
        return ResponseEntity.ok(billingService.getBillingStats());
    }
}
