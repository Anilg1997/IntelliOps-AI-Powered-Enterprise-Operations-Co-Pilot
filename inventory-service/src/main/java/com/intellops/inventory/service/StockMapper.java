package com.intellops.inventory.service;

import com.intellops.inventory.grpc.*;
import lombok.experimental.UtilityClass;

/**
 * Utility class for mapping between gRPC protobuf messages and internal domain results.
 */
@UtilityClass
public class StockMapper {

    public static StockCheckResponse toCheckResponse(InventoryService.StockCheckResult result) {
        return StockCheckResponse.newBuilder()
                .setAvailable(result.isAvailable())
                .setAvailableQuantity(result.getAvailableQuantity())
                .setWarehouseLocation(result.getWarehouseLocation() != null ? result.getWarehouseLocation() : "")
                .setEstimatedRestockDate(result.getEstimatedRestockDate() != null ? result.getEstimatedRestockDate() : "")
                .build();
    }

    public static StockReserveResponse toReserveResponse(InventoryService.StockReserveResult result) {
        return StockReserveResponse.newBuilder()
                .setSuccess(result.isSuccess())
                .setMessage(result.getMessage())
                .setRemainingQuantity(result.getRemainingQuantity())
                .build();
    }

    public static StockReleaseResponse toReleaseResponse(InventoryService.StockReleaseResult result) {
        return StockReleaseResponse.newBuilder()
                .setSuccess(result.isSuccess())
                .setMessage(result.getMessage())
                .setAvailableQuantity(result.getAvailableQuantity())
                .build();
    }

    public static StockDetailsResponse toDetailsResponse(InventoryService.StockDetailsResult result) {
        return StockDetailsResponse.newBuilder()
                .setSku(result.getSku() != null ? result.getSku() : "")
                .setProductName(result.getProductName() != null ? result.getProductName() : "")
                .setTotalQuantity(result.getTotalQuantity())
                .setReservedQuantity(result.getReservedQuantity())
                .setAvailableQuantity(result.getAvailableQuantity())
                .setWarehouseLocation(result.getWarehouseLocation() != null ? result.getWarehouseLocation() : "")
                .setRestockDate(result.getRestockDate() != null ? result.getRestockDate() : "")
                .setStatus(result.getStatus() != null ? result.getStatus() : "")
                .build();
    }
}
