package com.intellops.order.grpc;

import com.intellops.inventory.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * gRPC client for communicating with the Inventory Service's StockService.
 * <p>
 * All calls are wrapped in try-catch to gracefully degrade when the inventory
 * service is unavailable. If the gRPC call fails, a fallback response is returned
 * indicating that stock information is unavailable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockServiceClient {

    @GrpcClient("inventory-service")
    private StockServiceGrpc.StockServiceBlockingStub stockServiceStub;

    /**
     * Check if sufficient stock is available for a given SKU and quantity.
     *
     * @return StockCheckResult with availability info, or fallback if unavailable
     */
    public StockCheckResult checkStock(String sku, int quantity) {
        try {
            StockCheckRequest request = StockCheckRequest.newBuilder()
                    .setSku(sku)
                    .setQuantity(quantity)
                    .build();

            StockCheckResponse response = stockServiceStub.checkStock(request);
            log.debug("Stock check for SKU '{}' (qty {}): available={}, remaining={}",
                    sku, quantity, response.getAvailable(), response.getAvailableQuantity());

            return StockCheckResult.fromGrpc(response);
        } catch (StatusRuntimeException e) {
            log.warn("gRPC checkStock failed for SKU '{}': {} — using fallback", sku, e.getStatus().getDescription());
            return StockCheckResult.fallback();
        } catch (Exception e) {
            log.error("Unexpected error checking stock for SKU '{}': {}", sku, e.getMessage());
            return StockCheckResult.fallback();
        }
    }

    /**
     * Reserve stock for an order.
     *
     * @return ReserveResult indicating success/failure, or fallback if unavailable
     */
    public ReserveResult reserveStock(String sku, int quantity, String orderNumber) {
        try {
            StockReserveRequest request = StockReserveRequest.newBuilder()
                    .setSku(sku)
                    .setQuantity(quantity)
                    .setOrderNumber(orderNumber)
                    .build();

            StockReserveResponse response = stockServiceStub.reserveStock(request);
            log.info("Stock reserved for SKU '{}' (qty {}) for order {}: success={}",
                    sku, quantity, orderNumber, response.getSuccess());

            return new ReserveResult(response.getSuccess(), response.getMessage(),
                    response.getRemainingQuantity());
        } catch (StatusRuntimeException e) {
            log.warn("gRPC reserveStock failed for SKU '{}', order {}: {}",
                    sku, orderNumber, e.getStatus().getDescription());
            return new ReserveResult(false, "Inventory service unavailable: " + e.getStatus().getDescription(), 0);
        } catch (Exception e) {
            log.error("Unexpected error reserving stock for SKU '{}': {}", sku, e.getMessage());
            return new ReserveResult(false, "Unexpected error: " + e.getMessage(), 0);
        }
    }

    /**
     * Release reserved stock back to inventory.
     */
    public boolean releaseStock(String sku, int quantity, String orderNumber, String reason) {
        try {
            StockReleaseRequest request = StockReleaseRequest.newBuilder()
                    .setSku(sku)
                    .setQuantity(quantity)
                    .setOrderNumber(orderNumber)
                    .setReason(reason)
                    .build();

            StockReleaseResponse response = stockServiceStub.releaseStock(request);
            log.info("Stock released for SKU '{}' (qty {}) from order {}: success={}",
                    sku, quantity, orderNumber, response.getSuccess());
            return response.getSuccess();
        } catch (StatusRuntimeException e) {
            log.warn("gRPC releaseStock failed for SKU '{}', order {}: {}",
                    sku, orderNumber, e.getStatus().getDescription());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error releasing stock for SKU '{}': {}", sku, e.getMessage());
            return false;
        }
    }

    /**
     * Result of a stock check operation.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StockCheckResult {
        private boolean available;
        private int availableQuantity;
        private String warehouseLocation;
        private String estimatedRestockDate;

        /**
         * Creates a fallback result when the inventory service is unreachable.
         */
        public static StockCheckResult fallback() {
            return StockCheckResult.builder()
                    .available(true) // optimistic — allow the order to proceed
                    .availableQuantity(999)
                    .warehouseLocation("unknown")
                    .estimatedRestockDate("")
                    .build();
        }

        /**
         * Creates a result from a gRPC response.
         */
        public static StockCheckResult fromGrpc(StockCheckResponse response) {
            return StockCheckResult.builder()
                    .available(response.getAvailable())
                    .availableQuantity(response.getAvailableQuantity())
                    .warehouseLocation(response.getWarehouseLocation())
                    .estimatedRestockDate(response.getEstimatedRestockDate())
                    .build();
        }
    }

    /**
     * Result of a stock reserve operation.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ReserveResult {
        private boolean success;
        private String message;
        private int remainingQuantity;
    }
}
