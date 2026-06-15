package com.intellops.copilot.service.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP Tool — Order Service integration.
 * <p>
 * Allows the AI agent to look up order details by order number,
 * check order status, and retrieve customer/line-item information.
 * <p>
 * Queries the Order Service's PostgreSQL database directly (read-only)
 * for fast, low-latency lookup without going through the REST API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool("Look up order details by order number. Returns status, total amount, customer info, and line items.")
    public String getOrderDetails(String orderNumber) {
        log.info("🔧 MCP Tool: getOrderDetails({})", orderNumber);
        try {
            // Fetch order + customer info
            String orderSql = """
                    SELECT o.order_number, o.status, o.status_reason, o.total_amount,
                           o.created_at, o.updated_at,
                           c.name AS customer_name, c.email AS customer_email
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.id
                    WHERE o.order_number = ?
                    """;

            Map<String, Object> order = jdbcTemplate.queryForMap(orderSql, orderNumber);

            // Fetch line items
            String itemsSql = """
                    SELECT li.quantity, li.unit_price, li.subtotal,
                           p.name AS product_name, p.sku AS product_sku
                    FROM order_line_items li
                    JOIN products p ON li.product_id = p.id
                    WHERE li.order_id = (SELECT id FROM orders WHERE order_number = ?)
                    """;

            StringBuilder result = new StringBuilder();
            result.append("📋 Order: ").append(order.get("order_number")).append("\n");
            result.append("Status: ").append(order.get("status"));
            if (order.get("status_reason") != null) {
                result.append(" (").append(order.get("status_reason")).append(")");
            }
            result.append("\n");
            result.append("Total Amount: ₹").append(order.get("total_amount")).append("\n");
            result.append("Customer: ").append(order.get("customer_name")).append(" (").append(order.get("customer_email")).append(")\n");
            result.append("Created: ").append(order.get("created_at")).append("\n");

            result.append("\nLine Items:\n");
            jdbcTemplate.query(itemsSql, new Object[]{orderNumber}, (rs) -> {
                result.append("  - ").append(rs.getString("product_name"))
                        .append(" (SKU: ").append(rs.getString("product_sku")).append(")")
                        .append(" × ").append(rs.getInt("quantity"))
                        .append(" @ ₹").append(rs.getBigDecimal("unit_price"))
                        .append(" = ₹").append(rs.getBigDecimal("subtotal"))
                        .append("\n");
            });

            return result.toString();
        } catch (Exception e) {
            log.error("Error fetching order {}: {}", orderNumber, e.getMessage());
            return "❌ Order not found: " + orderNumber;
        }
    }

    @Tool("Search for orders by customer email. Returns a summary of the customer's recent orders.")
    public String searchOrdersByCustomer(String email) {
        log.info("🔧 MCP Tool: searchOrdersByCustomer({})", email);
        try {
            String sql = """
                    SELECT o.order_number, o.status, o.total_amount, o.created_at
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.id
                    WHERE c.email = ?
                    ORDER BY o.created_at DESC
                    LIMIT 10
                    """;

            StringBuilder result = new StringBuilder();
            result.append("Orders for ").append(email).append(":\n");

            int[] count = {0};
            jdbcTemplate.query(sql, new Object[]{email}, (rs) -> {
                result.append("  ").append(rs.getString("order_number"))
                        .append(" | ").append(rs.getString("status"))
                        .append(" | ₹").append(rs.getBigDecimal("total_amount"))
                        .append(" | ").append(rs.getTimestamp("created_at").toLocalDateTime())
                        .append("\n");
                count[0]++;
            });

            if (count[0] == 0) {
                return "No orders found for customer: " + email;
            }
            result.insert(0, count[0] + " order(s) found:\n");
            return result.toString();
        } catch (Exception e) {
            log.error("Error searching orders for {}: {}", email, e.getMessage());
            return "❌ Error searching orders: " + e.getMessage();
        }
    }

    @Tool("Get a summary of order statistics: total orders, pending orders, on-hold orders, total revenue.")
    public String getOrderStatistics() {
        log.info("🔧 MCP Tool: getOrderStatistics()");
        try {
            String sql = """
                    SELECT
                        COUNT(*) AS total_orders,
                        COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
                        COUNT(*) FILTER (WHERE status = 'ON_HOLD') AS on_hold,
                        COUNT(*) FILTER (WHERE status IN ('SHIPPED','DELIVERED')) AS completed,
                        COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled,
                        COALESCE(SUM(total_amount) FILTER (WHERE status NOT IN ('CANCELLED','REFUNDED')), 0) AS total_revenue
                    FROM orders
                    """;

            Map<String, Object> stats = jdbcTemplate.queryForMap(sql);

            return String.format("""
                    📊 Order Statistics:
                    • Total Orders: %s
                    • Pending: %s
                    • On Hold: %s
                    • Completed: %s
                    • Cancelled: %s
                    • Total Revenue: ₹%s
                    """,
                    stats.get("total_orders"), stats.get("pending"), stats.get("on_hold"),
                    stats.get("completed"), stats.get("cancelled"), stats.get("total_revenue"));
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage());
            return "❌ Error fetching statistics: " + e.getMessage();
        }
    }
}
