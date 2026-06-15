package com.intellops.inventory.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Product catalog document stored in MongoDB.
 * Uses flexible document model so each product can have exactly the attributes it needs.
 * <p>
 * Example:
 * <pre>
 * { "category": "electronics", "specs": { "ram": "16GB", "storage": "512GB" } }
 * { "category": "clothing", "specs": { "size": "M", "color": "Navy Blue", "material": "Cotton" } }
 * </pre>
 */
@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("sku")
    private String sku;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("category")
    private String category;

    @Field("price")
    private BigDecimal price;

    /**
     * Flexible specs map — varies by category.
     * Electronics: { "ram", "storage", "processor" }
     * Clothing: { "size", "color", "material" }
     * Services: { "duration", "support_level" }
     */
    @Field("specs")
    private Map<String, String> specs;

    @Field("tags")
    private java.util.List<String> tags;

    @Field("active")
    private boolean active;

    @Field("image_url")
    private String imageUrl;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
