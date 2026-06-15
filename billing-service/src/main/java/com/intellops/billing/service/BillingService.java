package com.intellops.billing.service;

import com.intellops.billing.config.BillingEventPublisher;
import com.intellops.billing.dto.BillingAccountDto;
import com.intellops.billing.dto.InvoiceDto;
import com.intellops.billing.dto.PaymentDto;
import com.intellops.billing.entity.*;
import com.intellops.billing.exception.ResourceNotFoundException;
import com.intellops.billing.repository.BillingAccountRepository;
import com.intellops.billing.repository.InvoiceRepository;
import com.intellops.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillingAccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BillingEventPublisher eventPublisher;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ─── Billing Accounts ─────────────────────────────────────────────────

    @Transactional
    public BillingAccountDto.Response createAccount(BillingAccountDto.CreateRequest request) {
        if (accountRepository.existsByCustomerEmail(request.getCustomerEmail())) {
            throw new IllegalArgumentException("Account already exists for email: " + request.getCustomerEmail());
        }

        BillingAccount account = BillingAccount.builder()
                .accountNumber(generateAccountNumber())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.valueOf(50000))
                .build();

        account = accountRepository.save(account);
        log.info("✅ Created billing account: {} for customer: {}", account.getAccountNumber(), account.getCustomerEmail());
        return toAccountResponse(account);
    }

    public BillingAccountDto.Response getAccountByNumber(String accountNumber) {
        BillingAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount", "accountNumber", accountNumber));
        return toAccountResponse(account);
    }

    public BillingAccountDto.Response getAccountByEmail(String email) {
        BillingAccount account = accountRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount", "customerEmail", email));
        return toAccountResponse(account);
    }

    public List<BillingAccountDto.Response> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toAccountResponse)
                .toList();
    }

    /**
     * Retrieves the billing account entity by email (used internally by invoice/payment flows).
     */
    public BillingAccount getAccountEntityByEmail(String email) {
        return accountRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount", "customerEmail", email));
    }

    /**
     * Retrieves the billing account entity by account number.
     */
    public BillingAccount getAccountEntityByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount", "accountNumber", accountNumber));
    }

    // ─── Invoices ─────────────────────────────────────────────────────────

    @Transactional
    public InvoiceDto.Response createInvoice(String customerEmail, InvoiceDto.CreateRequest request) {
        BillingAccount account = getAccountEntityByEmail(customerEmail);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .orderNumber(request.getOrderNumber())
                .account(account)
                .status(InvoiceStatus.PENDING)
                .amount(request.getAmount())
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(30))
                .description(request.getDescription())
                .build();

        invoice = invoiceRepository.save(invoice);

        // Update account balance
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        log.info("📄 Created invoice: {} for order: {} (amount: ₹{})",
                invoice.getInvoiceNumber(), request.getOrderNumber(), request.getAmount());

        // Publish Kafka event
        eventPublisher.publishInvoiceCreated(invoice, account);

        return toInvoiceResponse(invoice);
    }

    public InvoiceDto.Response getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
        return toInvoiceResponse(invoice);
    }

    public List<InvoiceDto.Response> getInvoicesByAccount(String accountNumber) {
        BillingAccount account = getAccountEntityByNumber(accountNumber);
        return invoiceRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    public List<InvoiceDto.Response> getInvoicesByOrder(String orderNumber) {
        return invoiceRepository.findByOrderNumber(orderNumber).stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    public List<InvoiceDto.Response> getOverdueInvoices() {
        return invoiceRepository.findByStatus(InvoiceStatus.OVERDUE).stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    // ─── Payments ─────────────────────────────────────────────────────────

    @Transactional
    public PaymentDto.Response processPayment(PaymentDto.CreateRequest request) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", request.getInvoiceNumber()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Invoice " + request.getInvoiceNumber() + " is already paid");
        }

        BillingAccount account = invoice.getAccount();

        Payment payment = Payment.builder()
                .paymentRef(generatePaymentRef())
                .invoiceNumber(request.getInvoiceNumber())
                .account(account)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .status("COMPLETED")
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Update invoice
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.getAmount());
        invoice.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(invoice.getAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDate.now());
        }

        invoiceRepository.save(invoice);

        // Update account balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        log.info("💰 Processed payment: {} for invoice: {} (amount: ₹{})",
                payment.getPaymentRef(), request.getInvoiceNumber(), request.getAmount());

        // Publish Kafka events
        eventPublisher.publishPaymentReceived(payment, invoice, account);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            eventPublisher.publishInvoicePaid(invoice, payment, account);
        }

        return toPaymentResponse(payment);
    }

    public List<PaymentDto.Response> getPaymentsByInvoice(String invoiceNumber) {
        return paymentRepository.findByInvoiceNumber(invoiceNumber).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    public List<PaymentDto.Response> getPaymentsByAccount(String accountNumber) {
        BillingAccount account = getAccountEntityByNumber(accountNumber);
        return paymentRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    // ─── Helpers & Mappers ────────────────────────────────────────────────

    private String generateAccountNumber() {
        return "ACC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePaymentRef() {
        return "PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BillingAccountDto.Response toAccountResponse(BillingAccount account) {
        return BillingAccountDto.Response.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerEmail(account.getCustomerEmail())
                .customerName(account.getCustomerName())
                .status(account.getStatus().name())
                .balance(account.getBalance())
                .creditLimit(account.getCreditLimit())
                .createdAt(account.getCreatedAt() != null ? account.getCreatedAt().format(DTF) : null)
                .updatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().format(DTF) : null)
                .build();
    }

    private InvoiceDto.Response toInvoiceResponse(Invoice invoice) {
        return InvoiceDto.Response.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderNumber(invoice.getOrderNumber())
                .accountNumber(invoice.getAccount().getAccountNumber())
                .customerEmail(invoice.getAccount().getCustomerEmail())
                .customerName(invoice.getAccount().getCustomerName())
                .status(invoice.getStatus().name())
                .amount(invoice.getAmount())
                .paidAmount(invoice.getPaidAmount())
                .dueDate(invoice.getDueDate() != null ? invoice.getDueDate().toString() : null)
                .paidDate(invoice.getPaidDate() != null ? invoice.getPaidDate().toString() : null)
                .description(invoice.getDescription())
                .createdAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DTF) : null)
                .updatedAt(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().format(DTF) : null)
                .build();
    }

    private PaymentDto.Response toPaymentResponse(Payment payment) {
        return PaymentDto.Response.builder()
                .id(payment.getId())
                .paymentRef(payment.getPaymentRef())
                .invoiceNumber(payment.getInvoiceNumber())
                .accountNumber(payment.getAccount().getAccountNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().format(DTF) : null)
                .build();
    }
}
