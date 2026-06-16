package com.intellops.inventory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Map<String, Object> attributes;

    @Field("stockQuantity")
    private Integer stockQuantity;

    @Field("reorderThreshold")
    private Integer reorderThreshold;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean needsReorder() {
        return stockQuantity != null && reorderThreshold != null && stockQuantity <= reorderThreshold;
    }
}
