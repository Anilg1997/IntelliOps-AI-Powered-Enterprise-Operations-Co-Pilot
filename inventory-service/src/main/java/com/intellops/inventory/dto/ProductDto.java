package com.intellops.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

        @NotBlank(message = "Category is required")
        private String category;

        private Map<String, String> specs;
        private List<String> tags;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String sku;
        private BigDecimal price;
        private String category;
        private Map<String, String> specs;
        private List<String> tags;
        private boolean active;
        private String imageUrl;
        private String createdAt;
        private String updatedAt;
    }
}
