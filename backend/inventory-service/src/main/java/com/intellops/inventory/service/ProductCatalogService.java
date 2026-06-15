package com.intellops.inventory.service;

import com.intellops.inventory.document.ProductDocument;
import com.intellops.inventory.dto.ProductDto;
import com.intellops.inventory.exception.DuplicateResourceException;
import com.intellops.inventory.exception.ResourceNotFoundException;
import com.intellops.inventory.repository.ProductCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogService {

    private final ProductCatalogRepository productCatalogRepository;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ProductDto.Response createProduct(ProductDto.Request request) {
        if (productCatalogRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product already exists with SKU: " + request.getSku());
        }

        ProductDocument product = ProductDocument.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .specs(request.getSpecs())
                .tags(request.getTags())
                .imageUrl(request.getImageUrl())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        product = productCatalogRepository.save(product);
        log.info("Created catalog product: {} (SKU: {})", product.getName(), product.getSku());
        return toResponse(product);
    }

    public ProductDto.Response getProduct(String id) {
        ProductDocument product = productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toResponse(product);
    }

    public ProductDto.Response getProductBySku(String sku) {
        ProductDocument product = productCatalogRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return toResponse(product);
    }

    public List<ProductDto.Response> getAllProducts() {
        return productCatalogRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductDto.Response> getProductsByCategory(String category) {
        return productCatalogRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductDto.Response updateProduct(String id, ProductDto.Request request) {
        ProductDocument product = productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getSpecs() != null) product.setSpecs(request.getSpecs());
        if (request.getTags() != null) product.setTags(request.getTags());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        product = productCatalogRepository.save(product);
        log.info("Updated catalog product: {} (SKU: {})", product.getName(), product.getSku());
        return toResponse(product);
    }

    public void deleteProduct(String id) {
        ProductDocument product = productCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productCatalogRepository.save(product);
        log.info("Deactivated catalog product: {} (SKU: {})", product.getName(), product.getSku());
    }

    public ProductDocument getProductEntityBySku(String sku) {
        return productCatalogRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    private ProductDto.Response toResponse(ProductDocument p) {
        return ProductDto.Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .sku(p.getSku())
                .price(p.getPrice())
                .category(p.getCategory())
                .specs(p.getSpecs())
                .tags(p.getTags())
                .active(p.isActive())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().format(DTF) : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().format(DTF) : null)
                .build();
    }
}
