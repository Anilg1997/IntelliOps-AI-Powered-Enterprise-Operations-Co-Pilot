package com.intellops.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class ProductDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotBlank(message = "SKU is required")
        private String sku;

        @Positive(message = "Price must be positive")
        private BigDecimal price;

        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String sku;
        private BigDecimal price;
        private String category;
        private String createdAt;
    }
}
