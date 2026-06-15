package com.intellops.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class PaymentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Invoice number is required")
        private String invoiceNumber;

        @NotNull(message = "Amount is required")
        @Positive
        private BigDecimal amount;

        @NotBlank(message = "Payment method is required")
        private String paymentMethod;

        private String transactionId;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String paymentRef;
        private String invoiceNumber;
        private String accountNumber;
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;
        private String status;
        private String notes;
        private String createdAt;
    }
}
