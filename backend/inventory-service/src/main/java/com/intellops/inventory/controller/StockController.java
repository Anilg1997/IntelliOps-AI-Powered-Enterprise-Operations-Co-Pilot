package com.intellops.inventory.controller;

import com.intellops.inventory.document.StockItem;
import com.intellops.inventory.dto.ApiResponse;
import com.intellops.inventory.dto.StockDto;
import com.intellops.inventory.dto.StockDto;
import com.intellops.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class StockController {

    private final InventoryService inventoryService;

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<List<StockDto.Response>>> getAllStock() {
        List<StockDto.Response> stockList = inventoryService.getAllStock().stream()
                .map(this::toStockResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(stockList));
    }

    @GetMapping("/stock/{sku}")
    public ResponseEntity<ApiResponse<StockDto.Response>> getStockBySku(@PathVariable String sku) {
        StockItem stock = inventoryService.getStockEntityBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(toStockResponse(stock)));
    }

    @GetMapping("/stock/{sku}/details")
    public ResponseEntity<ApiResponse<InventoryService.StockDetailsResult>> getStockDetails(
            @PathVariable String sku) {
        var details = inventoryService.getStockDetails(sku);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @PostMapping("/stock")
    public ResponseEntity<ApiResponse<StockDto.Response>> createStock(
            @Valid @RequestBody StockDto.CreateRequest request) {
        StockItem stock = StockItem.builder()
                .sku(request.getSku())
                .productName(request.getProductName())
                .totalQuantity(request.getTotalQuantity())
                .reservedQuantity(0)
                .warehouseLocation(request.getWarehouseLocation())
                .restockDate(request.getRestockDate() != null ? LocalDate.parse(request.getRestockDate()) : null)
                .reorderThreshold(request.getReorderThreshold() > 0 ? request.getReorderThreshold() : 10)
                .status(StockItem.StockStatus.IN_STOCK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        stock = inventoryService.saveStock(stock);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toStockResponse(stock), "Stock entry created"));
    }

    @PutMapping("/stock/{sku}")
    public ResponseEntity<ApiResponse<StockDto.Response>> updateStock(
            @PathVariable String sku,
            @RequestBody StockDto.UpdateRequest request) {
        StockItem stock = inventoryService.getStockEntityBySku(sku);

        if (request.getTotalQuantity() != null) stock.setTotalQuantity(request.getTotalQuantity());
        if (request.getWarehouseLocation() != null) stock.setWarehouseLocation(request.getWarehouseLocation());
        if (request.getRestockDate() != null) stock.setRestockDate(LocalDate.parse(request.getRestockDate()));
        if (request.getReorderThreshold() != null) stock.setReorderThreshold(request.getReorderThreshold());
        if (request.getStatus() != null) {
            stock.setStatus(StockItem.StockStatus.valueOf(request.getStatus()));
        }

        stock = inventoryService.saveStock(stock);
        return ResponseEntity.ok(ApiResponse.success(toStockResponse(stock), "Stock updated"));
    }

    @PostMapping("/stock/{sku}/check")
    public ResponseEntity<ApiResponse<InventoryService.StockCheckResult>> checkStock(
            @PathVariable String sku,
            @RequestParam int quantity) {
        var result = inventoryService.checkStock(sku, quantity);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/stock/{sku}/reserve")
    public ResponseEntity<ApiResponse<InventoryService.StockReserveResult>> reserveStock(
            @PathVariable String sku,
            @RequestParam int quantity,
            @RequestParam String orderNumber) {
        var result = inventoryService.reserveStock(sku, quantity, orderNumber);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock reserved"));
    }

    @PostMapping("/stock/{sku}/release")
    public ResponseEntity<ApiResponse<InventoryService.StockReleaseResult>> releaseStock(
            @PathVariable String sku,
            @RequestParam int quantity,
            @RequestParam String orderNumber,
            @RequestParam String reason) {
        var result = inventoryService.releaseStock(sku, quantity, orderNumber, reason);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock released"));
    }

    private StockDto.Response toStockResponse(StockItem stock) {
        return StockDto.Response.builder()
                .id(stock.getId())
                .sku(stock.getSku())
                .productName(stock.getProductName())
                .totalQuantity(stock.getTotalQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .warehouseLocation(stock.getWarehouseLocation())
                .restockDate(stock.getRestockDate() != null ? stock.getRestockDate().toString() : null)
                .status(stock.getStatus() != null ? stock.getStatus().name() : null)
                .reorderThreshold(stock.getReorderThreshold())
                .createdAt(stock.getCreatedAt() != null ? stock.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(stock.getUpdatedAt() != null ? stock.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
}
