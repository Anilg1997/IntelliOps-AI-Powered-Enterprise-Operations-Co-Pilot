package com.intellops.copilot.service.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP Tool — Inventory Service integration.
 * <p>
 * Allows the AI agent to check stock levels, find low-stock products,
 * and get product catalog information by querying the Inventory Service's
 * MongoDB data via REST or direct lookup.
 * <p>
 * Note: Currently queries a product/stock summary table. In production,
 * this would call the Inventory Service's gRPC or REST API.
 * For Phase 3, it reads from a stock_snapshot table populated by the
 * Inventory Service's Kafka events (to be implemented in Phase 4).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool("Check stock availability for a product by SKU. Returns quantity, warehouse, and status.")
    public String checkStockBySku(String sku) {
        log.info("🔧 MCP Tool: checkStockBySku({})", sku);
        try {
            String sql = """
                    SELECT p.name, p.sku, p.price, p.category,
                           COALESCE(s.available_qty, 0) AS available,
                           COALESCE(s.reserved_qty, 0) AS reserved,
                           s.warehouse, s.restock_date, s.status
                    FROM products p
                    LEFT JOIN stock_snapshot s ON p.sku = s.sku
                    WHERE p.sku = ?
                    """;

            Map<String, Object> result = jdbcTemplate.queryForMap(sql, sku);

            StringBuilder sb = new StringBuilder();
            sb.append("📦 Product: ").append(result.get("name")).append("\n");
            sb.append("SKU: ").append(result.get("sku")).append("\n");
            sb.append("Category: ").append(result.get("category")).append("\n");
            sb.append("Price: ₹").append(result.get("price")).append("\n");
            sb.append("Available Stock: ").append(result.get("available")).append(" units\n");
            sb.append("Reserved: ").append(result.get("reserved")).append(" units\n");
            sb.append("Warehouse: ").append(result.get("warehouse") != null ? result.get("warehouse") : "N/A").append("\n");
            sb.append("Stock Status: ").append(result.get("status") != null ? result.get("status") : "Unknown");

            if (result.get("restock_date") != null) {
                sb.append("\nEst. Restock Date: ").append(result.get("restock_date"));
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("Stock snapshot not found for SKU '{}', trying inventory service: {}", sku, e.getMessage());
            // Fallback: try Inventory Service REST API
            return "Stock information for SKU '" + sku + "' is currently unavailable via snapshot. "
                    + "Please check the Inventory Service directly at http://localhost:8082/api/v1/inventory/stock/" + sku;
        }
    }

    @Tool("Find products that are low on stock (below reorder threshold). Returns a list of items needing restock.")
    public String findLowStockProducts() {
        log.info("🔧 MCP Tool: findLowStockProducts()");
        try {
            String sql = """
                    SELECT p.name, p.sku, COALESCE(s.available_qty, 0) AS available,
                           COALESCE(s.reorder_threshold, 10) AS threshold
                    FROM products p
                    LEFT JOIN stock_snapshot s ON p.sku = s.sku
                    WHERE COALESCE(s.available_qty, 0) <= COALESCE(s.reorder_threshold, 10)
                    ORDER BY available ASC
                    LIMIT 20
                    """;

            StringBuilder result = new StringBuilder();
            result.append("⚠️ Low Stock Products:\n");

            int[] count = {0};
            jdbcTemplate.query(sql, (rs) -> {
                result.append("  - ").append(rs.getString("name"))
                        .append(" (SKU: ").append(rs.getString("sku")).append(")")
                        .append(": ").append(rs.getInt("available"))
                        .append(" units (threshold: ").append(rs.getInt("threshold")).append(")")
                        .append("\n");
                count[0]++;
            });

            if (count[0] == 0) {
                return "✅ All products are adequately stocked. No items below reorder threshold.";
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Error finding low stock products: {}", e.getMessage());
            return "❌ Error checking stock levels: " + e.getMessage();
        }
    }
}
