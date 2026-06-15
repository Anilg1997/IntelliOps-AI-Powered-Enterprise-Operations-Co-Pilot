package com.intellops.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Order number is required")
        private String orderNumber;

        @NotNull(message = "Amount is required")
        @Positive
        private BigDecimal amount;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String invoiceNumber;
        private String orderNumber;
        private String accountNumber;
        private String customerEmail;
        private String customerName;
        private String status;
        private BigDecimal amount;
        private BigDecimal paidAmount;
        private String dueDate;
        private String paidDate;
        private String description;
        private String createdAt;
        private String updatedAt;
    }
}
