package com.intellops.inventory.controller;

import com.intellops.inventory.dto.ApiResponse;
import com.intellops.inventory.dto.ProductDto;
import com.intellops.inventory.service.ProductCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDto.Response>> createProduct(
            @Valid @RequestBody ProductDto.Request request) {
        ProductDto.Response response = productCatalogService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created in catalog"));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getAllProducts() {
        List<ProductDto.Response> products = productCatalogService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> getProduct(@PathVariable String id) {
        ProductDto.Response response = productCatalogService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> getProductBySku(@PathVariable String sku) {
        ProductDto.Response response = productCatalogService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDto.Response>>> getProductsByCategory(
            @PathVariable String category) {
        List<ProductDto.Response> products = productCatalogService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto.Response>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDto.Request request) {
        ProductDto.Response response = productCatalogService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productCatalogService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deactivated"));
    }
}
