package com.intellops.inventory.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stock keeping document — tracks available, reserved, and incoming inventory per SKU.
 */
@Document(collection = "stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("sku")
    private String sku;

    @Field("product_name")
    private String productName;

    @Field("total_quantity")
    private int totalQuantity;

    @Field("reserved_quantity")
    private int reservedQuantity;

    @Field("warehouse_location")
    private String warehouseLocation;

    @Field("restock_date")
    private LocalDate restockDate;

    @Field("status")
    private StockStatus status;

    @Field("reorder_threshold")
    private int reorderThreshold;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Returns the quantity available for sale (total - reserved).
     */
    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }

    public enum StockStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED
    }
}
