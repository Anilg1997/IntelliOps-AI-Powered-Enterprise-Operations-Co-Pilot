package com.intellops.billing.controller;

import com.intellops.billing.dto.ApiResponse;
import com.intellops.billing.dto.BillingAccountDto;
import com.intellops.billing.dto.InvoiceDto;
import com.intellops.billing.dto.PaymentDto;
import com.intellops.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // ─── Accounts ─────────────────────────────────────────────────────────

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<BillingAccountDto.Response>> createAccount(
            @Valid @RequestBody BillingAccountDto.CreateRequest request) {
        BillingAccountDto.Response response = billingService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Billing account created"));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<BillingAccountDto.Response>> getAccount(
            @PathVariable String accountNumber) {
        BillingAccountDto.Response response = billingService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/by-email/{email}")
    public ResponseEntity<ApiResponse<BillingAccountDto.Response>> getAccountByEmail(
            @PathVariable String email) {
        BillingAccountDto.Response response = billingService.getAccountByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<BillingAccountDto.Response>>> getAllAccounts() {
        List<BillingAccountDto.Response> accounts = billingService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    // ─── Invoices ─────────────────────────────────────────────────────────

    @PostMapping("/accounts/{email}/invoices")
    public ResponseEntity<ApiResponse<InvoiceDto.Response>> createInvoice(
            @PathVariable String email,
            @Valid @RequestBody InvoiceDto.CreateRequest request) {
        InvoiceDto.Response response = billingService.createInvoice(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invoice created"));
    }

    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceDto.Response>> getInvoice(
            @PathVariable String invoiceNumber) {
        InvoiceDto.Response response = billingService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/{accountNumber}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDto.Response>>> getAccountInvoices(
            @PathVariable String accountNumber) {
        List<InvoiceDto.Response> invoices = billingService.getInvoicesByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/orders/{orderNumber}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDto.Response>>> getOrderInvoices(
            @PathVariable String orderNumber) {
        List<InvoiceDto.Response> invoices = billingService.getInvoicesByOrder(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/invoices/overdue")
    public ResponseEntity<ApiResponse<List<InvoiceDto.Response>>> getOverdueInvoices() {
        List<InvoiceDto.Response> invoices = billingService.getOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    // ─── Payments ─────────────────────────────────────────────────────────

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> processPayment(
            @Valid @RequestBody PaymentDto.CreateRequest request) {
        PaymentDto.Response response = billingService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment processed"));
    }

    @GetMapping("/invoices/{invoiceNumber}/payments")
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> getInvoicePayments(
            @PathVariable String invoiceNumber) {
        List<PaymentDto.Response> payments = billingService.getPaymentsByInvoice(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/accounts/{accountNumber}/payments")
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> getAccountPayments(
            @PathVariable String accountNumber) {
        List<PaymentDto.Response> payments = billingService.getPaymentsByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
