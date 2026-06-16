package com.intellops.copilot.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class InventoryTool {

    private final WebClient webClient;

    public InventoryTool(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    @Tool("Check stock availability for a product by SKU or ID. Use this when a user asks about product availability.")
    public String checkStock(String productId, int quantity) {
        try {
            log.info("AI Tool: Checking stock for product: {} qty: {}", productId, quantity);
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/inventory/stock/{productId}")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return "Unable to check stock for product: " + productId;
            }

            boolean inStock = (Boolean) response.get("inStock");
            return String.format(
                    "Stock for %s: %s (Available: %s, Reserved: %s, Reorder: %s)",
                    productId,
                    inStock ? "IN STOCK" : "OUT OF STOCK",
                    response.get("availableQuantity"),
                    response.get("reservedQuantity"),
                    response.get("reorderStatus")
            );
        } catch (Exception e) {
            log.error("Failed to check stock: {}", e.getMessage());
            return "Error checking stock: " + e.getMessage();
        }
    }

    @Tool("Get product details by SKU or ID. Use this to find product information.")
    public String getProduct(String productId) {
        try {
            log.info("AI Tool: Getting product: {}", productId);
            Map<?, ?> response = webClient.get()
                    .uri("/api/v1/inventory/products/{productId}", productId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return "Product not found: " + productId;
            }

            return String.format(
                    "Product: %s (SKU: %s) - $%s, Category: %s, Stock: %s, Active: %s",
                    response.get("name"),
                    response.get("sku"),
                    response.get("price"),
                    response.get("category"),
                    response.get("stockQuantity"),
                    response.get("active")
            );
        } catch (Exception e) {
            log.error("Failed to get product: {}", e.getMessage());
            return "Error retrieving product: " + e.getMessage();
        }
    }

    @Tool("List all products with optional category filter.")
    public String listProducts(String category) {
        try {
            log.info("AI Tool: Listing products, category: {}", category);
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/inventory/products")
                            .queryParam("category", category != null ? category : "")
                            .queryParam("activeOnly", true)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("products") == null) {
                return "No products found.";
            }

            java.util.List<?> products = (java.util.List<?>) response.get("products");
            StringBuilder sb = new StringBuilder("Products:\n");
            for (Object p : products) {
                Map<?, ?> product = (Map<?, ?>) p;
                sb.append(String.format("- %s (SKU: %s) - $%s, Stock: %s\n",
                        product.get("name"),
                        product.get("sku"),
                        product.get("price"),
                        product.get("stockQuantity")));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to list products: {}", e.getMessage());
            return "Error listing products: " + e.getMessage();
        }
    }
}
