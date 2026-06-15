package com.intellops.inventory.grpc;

import com.intellops.inventory.exception.InsufficientStockException;
import com.intellops.inventory.exception.ResourceNotFoundException;
import com.intellops.inventory.service.InventoryService;
import com.intellops.inventory.service.StockMapper;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class StockGrpcService extends StockServiceGrpc.StockServiceImplBase {

    private final InventoryService inventoryService;

    @Override
    public void checkStock(StockCheckRequest request, StreamObserver<StockCheckResponse> responseObserver) {
        try {
            log.debug("gRPC checkStock: SKU={}, quantity={}", request.getSku(), request.getQuantity());
            var result = inventoryService.checkStock(request.getSku(), request.getQuantity());
            responseObserver.onNext(StockMapper.toCheckResponse(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in checkStock for SKU {}: {}", request.getSku(), e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to check stock: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void reserveStock(StockReserveRequest request, StreamObserver<StockReserveResponse> responseObserver) {
        try {
            log.info("gRPC reserveStock: SKU={}, quantity={}, order={}",
                    request.getSku(), request.getQuantity(), request.getOrderNumber());
            var result = inventoryService.reserveStock(
                    request.getSku(), request.getQuantity(), request.getOrderNumber());
            responseObserver.onNext(StockMapper.toReserveResponse(result));
            responseObserver.onCompleted();
        } catch (InsufficientStockException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Error in reserveStock for SKU {}: {}", request.getSku(), e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to reserve stock: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void releaseStock(StockReleaseRequest request, StreamObserver<StockReleaseResponse> responseObserver) {
        try {
            log.info("gRPC releaseStock: SKU={}, quantity={}, order={}, reason={}",
                    request.getSku(), request.getQuantity(), request.getOrderNumber(), request.getReason());
            var result = inventoryService.releaseStock(
                    request.getSku(), request.getQuantity(), request.getOrderNumber(), request.getReason());
            responseObserver.onNext(StockMapper.toReleaseResponse(result));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Error in releaseStock for SKU {}: {}", request.getSku(), e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to release stock: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getStockDetails(StockDetailsRequest request, StreamObserver<StockDetailsResponse> responseObserver) {
        try {
            log.debug("gRPC getStockDetails: SKU={}", request.getSku());
            var result = inventoryService.getStockDetails(request.getSku());
            responseObserver.onNext(StockMapper.toDetailsResponse(result));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Error in getStockDetails for SKU {}: {}", request.getSku(), e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get stock details: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
