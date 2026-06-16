package com.intellops.inventory.controller;

import com.intellops.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class InventoryRestController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(inventoryService.listProducts(category, activeOnly, page, pageSize));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getProduct(productId));
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(inventoryService.checkStock(productId, quantity));
    }
}
