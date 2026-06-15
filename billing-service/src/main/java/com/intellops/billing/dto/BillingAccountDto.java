package com.intellops.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class BillingAccountDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Customer email is required")
        @Email
        private String customerEmail;

        @NotBlank(message = "Customer name is required")
        private String customerName;

        private BigDecimal creditLimit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String accountNumber;
        private String customerEmail;
        private String customerName;
        private String status;
        private BigDecimal balance;
        private BigDecimal creditLimit;
        private String createdAt;
        private String updatedAt;
    }
}
