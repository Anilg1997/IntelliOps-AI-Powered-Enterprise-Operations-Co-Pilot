package com.intellops.inventory.service;

import com.intellops.inventory.document.StockItem;
import com.intellops.inventory.exception.InsufficientStockException;
import com.intellops.inventory.exception.ResourceNotFoundException;
import com.intellops.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final StockRepository stockRepository;

    /**
     * Check if sufficient stock is available for the given SKU and quantity.
     */
    public StockCheckResult checkStock(String sku, int quantity) {
        Optional<StockItem> stockOpt = stockRepository.findBySku(sku);

        if (stockOpt.isEmpty()) {
            return StockCheckResult.unavailable(0, null, null);
        }

        StockItem stock = stockOpt.get();
        int available = stock.getAvailableQuantity();
        boolean availableFlag = available >= quantity;

        return StockCheckResult.builder()
                .available(availableFlag)
                .availableQuantity(available)
                .warehouseLocation(stock.getWarehouseLocation())
                .estimatedRestockDate(stock.getRestockDate() != null ? stock.getRestockDate().toString() : null)
                .status(stock.getStatus())
                .build();
    }

    /**
     * Reserve stock for an order. Throws InsufficientStockException if not enough available.
     */
    public StockReserveResult reserveStock(String sku, int quantity, String orderNumber) {
        StockItem stock = stockRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "sku", sku));

        int available = stock.getAvailableQuantity();
        if (available < quantity) {
            log.warn("Stock reservation failed for SKU '{}': requested {}, available {} (order: {})",
                    sku, quantity, available, orderNumber);
            throw new InsufficientStockException(sku, quantity, available);
        }

        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        updateStockStatus(stock);
        stock.setUpdatedAt(LocalDateTime.now());
        stock = stockRepository.save(stock);

        log.info("Reserved {} units of SKU '{}' for order {} (remaining available: {})",
                quantity, sku, orderNumber, stock.getAvailableQuantity());

        return StockReserveResult.builder()
                .success(true)
                .message(String.format("Reserved %d units of SKU '%s'", quantity, sku))
                .remainingQuantity(stock.getAvailableQuantity())
                .build();
    }

    /**
     * Release reserved stock (when order is cancelled, payment fails, etc.).
     */
    public StockReleaseResult releaseStock(String sku, int quantity, String orderNumber, String reason) {
        StockItem stock = stockRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "sku", sku));

        int newReserved = Math.max(0, stock.getReservedQuantity() - quantity);
        stock.setReservedQuantity(newReserved);
        updateStockStatus(stock);
        stock.setUpdatedAt(LocalDateTime.now());
        stock = stockRepository.save(stock);

        log.info("Released {} units of SKU '{}' from order {} (reason: {}, available: {})",
                quantity, sku, orderNumber, reason, stock.getAvailableQuantity());

        return StockReleaseResult.builder()
                .success(true)
                .message(String.format("Released %d units of SKU '%s'", quantity, sku))
                .availableQuantity(stock.getAvailableQuantity())
                .build();
    }

    /**
     * Get full stock details for a product.
     */
    public StockDetailsResult getStockDetails(String sku) {
        StockItem stock = stockRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "sku", sku));

        return StockDetailsResult.builder()
                .sku(stock.getSku())
                .productName(stock.getProductName())
                .totalQuantity(stock.getTotalQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .warehouseLocation(stock.getWarehouseLocation())
                .restockDate(stock.getRestockDate() != null ? stock.getRestockDate().toString() : null)
                .status(stock.getStatus().name())
                .build();
    }

    public List<StockItem> getAllStock() {
        return stockRepository.findAll();
    }

    public StockItem getStockEntityBySku(String sku) {
        return stockRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "sku", sku));
    }

    public StockItem saveStock(StockItem stock) {
        updateStockStatus(stock);
        if (stock.getCreatedAt() == null) {
            stock.setCreatedAt(LocalDateTime.now());
        }
        stock.setUpdatedAt(LocalDateTime.now());
        return stockRepository.save(stock);
    }

    private void updateStockStatus(StockItem stock) {
        int available = stock.getAvailableQuantity();
        if (available <= 0) {
            stock.setStatus(StockItem.StockStatus.OUT_OF_STOCK);
        } else if (available <= stock.getReorderThreshold()) {
            stock.setStatus(StockItem.StockStatus.LOW_STOCK);
        } else {
            stock.setStatus(StockItem.StockStatus.IN_STOCK);
        }
    }

    // ─── Result classes ─────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockCheckResult {
        private boolean available;
        private int availableQuantity;
        private String warehouseLocation;
        private String estimatedRestockDate;
        private StockItem.StockStatus status;

        public static StockCheckResult unavailable(int availableQty, String warehouse, String restockDate) {
            return StockCheckResult.builder()
                    .available(false)
                    .availableQuantity(availableQty)
                    .warehouseLocation(warehouse)
                    .estimatedRestockDate(restockDate)
                    .status(StockItem.StockStatus.OUT_OF_STOCK)
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockReserveResult {
        private boolean success;
        private String message;
        private int remainingQuantity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockReleaseResult {
        private boolean success;
        private String message;
        private int availableQuantity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockDetailsResult {
        private String sku;
        private String productName;
        private int totalQuantity;
        private int reservedQuantity;
        private int availableQuantity;
        private String warehouseLocation;
        private String restockDate;
        private String status;
    }
}
