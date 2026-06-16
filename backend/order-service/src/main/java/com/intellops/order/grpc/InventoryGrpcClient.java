package com.intellops.order.grpc;

import com.intellops.proto.inventory.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryGrpcClient {

    @GrpcClient("inventoryGrpcService")
    private InventoryGrpcServiceGrpc.InventoryGrpcServiceBlockingStub inventoryStub;

    public StockCheckResponse checkStock(String productId, int quantity) {
        try {
            StockCheckRequest request = StockCheckRequest.newBuilder()
                    .setProductId(productId)
                    .setRequestedQuantity(quantity)
                    .build();

            StockCheckResponse response = inventoryStub.checkStock(request);
            log.debug("Stock check for product {}: inStock={}, available={}",
                    productId, response.getInStock(), response.getAvailableQuantity());
            return response;
        } catch (Exception e) {
            log.error("Failed to check stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Inventory service unavailable: " + e.getMessage());
        }
    }

    public StockReserveResponse reserveStock(String orderId, String productId, int quantity) {
        try {
            StockReserveRequest request = StockReserveRequest.newBuilder()
                    .setOrderId(orderId)
                    .setProductId(productId)
                    .setQuantity(quantity)
                    .setReservationTtlMinutes(30)
                    .build();

            StockReserveResponse response = inventoryStub.reserveStock(request);
            log.debug("Stock reservation for order {}: success={}", orderId, response.getSuccess());
            return response;
        } catch (Exception e) {
            log.error("Failed to reserve stock for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Inventory service unavailable: " + e.getMessage());
        }
    }

    public StockReleaseResponse releaseStock(String reservationId, String orderId) {
        try {
            StockReleaseRequest request = StockReleaseRequest.newBuilder()
                    .setReservationId(reservationId)
                    .setOrderId(orderId)
                    .build();

            StockReleaseResponse response = inventoryStub.releaseStock(request);
            log.debug("Stock release for order {}: success={}", orderId, response.getSuccess());
            return response;
        } catch (Exception e) {
            log.error("Failed to release stock for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Inventory service unavailable: " + e.getMessage());
        }
    }
}
