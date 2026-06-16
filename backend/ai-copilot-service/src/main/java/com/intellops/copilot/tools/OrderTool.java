package com.intellops.copilot.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class OrderTool {

    private final WebClient webClient;

    @Value("${intellops.ai.tools.order-service-url}")
    private String orderServiceUrl;

    public OrderTool(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Tool("Get order details by order number. Use this when a user asks about a specific order status or details.")
    public String getOrder(String orderNumber) {
        try {
            log.info("AI Tool: Getting order details for: {}", orderNumber);
            Map<?, ?> response = webClient.get()
                    .uri("/api/v1/orders/{orderNumber}", orderNumber)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return "Order not found: " + orderNumber;
            }

            return String.format(
                    "Order #%s - Status: %s, Total: $%s, Tax: $%s, Customer: %s, Items: %d",
                    response.get("orderNumber"),
                    response.get("status"),
                    response.get("totalAmount"),
                    response.get("taxAmount"),
                    response.get("customer") != null ?
                            ((Map<?, ?>) response.get("customer")).get("name") : "Unknown",
                    response.get("lineItems") != null ?
                            ((java.util.List<?>) response.get("lineItems")).size() : 0
            );
        } catch (Exception e) {
            log.error("Failed to get order {}: {}", orderNumber, e.getMessage());
            return "Error retrieving order: " + e.getMessage();
        }
    }

    @Tool("List recent orders with optional search filter. Use this to find orders or get an overview.")
    public String listOrders(String search) {
        try {
            log.info("AI Tool: Listing orders with search: {}", search);
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/orders")
                            .queryParam("page", 0)
                            .queryParam("size", 10)
                            .queryParam("search", search != null ? search : "")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("content") == null) {
                return "No orders found.";
            }

            java.util.List<?> orders = (java.util.List<?>) response.get("content");
            StringBuilder sb = new StringBuilder("Recent orders:\n");
            for (Object o : orders) {
                Map<?, ?> order = (Map<?, ?>) o;
                sb.append(String.format("- #%s: %s ($%s)\n",
                        order.get("orderNumber"),
                        order.get("status"),
                        order.get("totalAmount")));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to list orders: {}", e.getMessage());
            return "Error listing orders: " + e.getMessage();
        }
    }

    @Tool("Get order statistics including total orders, revenue, and status breakdown.")
    public String getOrderStats() {
        try {
            log.info("AI Tool: Getting order statistics");
            Map<?, ?> stats = webClient.get()
                    .uri("/api/v1/orders/stats")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (stats == null) {
                return "Unable to retrieve order statistics.";
            }

            return String.format(
                    "Order Statistics:\n- Total Orders: %s\n- Pending: %s\n- Confirmed: %s\n" +
                    "- Processing: %s\n- Shipped: %s\n- Delivered: %s\n- Total Revenue: $%s",
                    stats.get("totalOrders"),
                    stats.get("pendingOrders"),
                    stats.get("confirmedOrders"),
                    stats.get("processingOrders"),
                    stats.get("shippedOrders"),
                    stats.get("deliveredOrders"),
                    stats.get("totalRevenue")
            );
        } catch (Exception e) {
            log.error("Failed to get order stats: {}", e.getMessage());
            return "Error retrieving statistics: " + e.getMessage();
        }
    }
}
