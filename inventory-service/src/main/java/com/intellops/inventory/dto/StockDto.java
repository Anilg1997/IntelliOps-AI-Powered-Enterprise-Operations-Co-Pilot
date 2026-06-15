package com.intellops.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class StockDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "SKU is required")
        private String sku;

        private String productName;

        @Positive(message = "Total quantity must be positive")
        private int totalQuantity;

        private String warehouseLocation;

        private String restockDate;

        private int reorderThreshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Positive(message = "Total quantity must be non-negative")
        private Integer totalQuantity;

        private String warehouseLocation;

        private String restockDate;

        private String status;

        private Integer reorderThreshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String sku;
        private String productName;
        private int totalQuantity;
        private int reservedQuantity;
        private int availableQuantity;
        private String warehouseLocation;
        private String restockDate;
        private String status;
        private int reorderThreshold;
        private String createdAt;
        private String updatedAt;
    }
}
