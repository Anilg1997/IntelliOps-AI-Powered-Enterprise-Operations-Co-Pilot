package com.intellops.copilot.service.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * MCP Tool — Legacy Billing Service integration (Phase 4).
 * <p>
 * Allows the AI agent to query billing accounts, invoices, and payments
 * by calling the billing-service REST API. The billing-service has its own
 * database (H2 Oracle mode locally, Oracle in Docker), so we communicate
 * via HTTP instead of direct JDBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingTool {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${intellops.billing.base-url:http://localhost:8084}")
    private String billingBaseUrl;

    @Tool("Get billing account details by account number or customer email. Returns account status, balance, credit limit, and recent invoices.")
    public String getAccountDetails(String accountNumberOrEmail) {
        log.info("🔧 MCP Tool: getAccountDetails({})", accountNumberOrEmail);
        try {
            // Try account number first, then email
            String json = httpGet(billingBaseUrl + "/api/api/v1/billing/accounts/" + accountNumberOrEmail);
            if (json == null || json.contains("error")) {
                json = httpGet(billingBaseUrl + "/api/api/v1/billing/accounts/by-email/" + accountNumberOrEmail);
            }
            if (json == null) return "❌ Billing service is unavailable. Please ensure billing-service is running on port 8084.";

            JsonNode root = objectMapper.readTree(json);
            if (!root.has("data") || root.get("data").isNull()) {
                return "❌ No billing account found for: " + accountNumberOrEmail;
            }

            JsonNode data = root.get("data");
            StringBuilder sb = new StringBuilder();
            sb.append("💳 Billing Account\n");
            sb.append("Account: ").append(getText(data, "accountNumber")).append("\n");
            sb.append("Customer: ").append(getText(data, "customerName")).append(" (").append(getText(data, "customerEmail")).append(")\n");
            sb.append("Status: ").append(getText(data, "status")).append("\n");
            sb.append("Balance: ₹").append(getNumber(data, "balance")).append("\n");
            sb.append("Credit Limit: ₹").append(getNumber(data, "creditLimit")).append("\n");

            // Fetch recent invoices for this account
            String accountNumber = getText(data, "accountNumber");
            String invoicesJson = httpGet(billingBaseUrl + "/api/api/v1/billing/accounts/" + accountNumber + "/invoices");
            if (invoicesJson != null) {
                JsonNode invoicesRoot = objectMapper.readTree(invoicesJson);
                if (invoicesRoot.has("data") && invoicesRoot.get("data").isArray() && invoicesRoot.get("data").size() > 0) {
                    sb.append("\nRecent Invoices:\n");
                    for (JsonNode inv : invoicesRoot.get("data")) {
                        sb.append("  - ").append(getText(inv, "invoiceNumber"))
                                .append(" | ").append(getText(inv, "status"))
                                .append(" | ₹").append(getNumber(inv, "amount"))
                                .append(" | Due: ").append(getText(inv, "dueDate"))
                                .append("\n");
                    }
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Error fetching account {}: {}", accountNumberOrEmail, e.getMessage());
            return "❌ Error fetching billing account: " + e.getMessage();
        }
    }

    @Tool("Get complete invoice details by invoice number. Returns status, amount, due date, payment info, and customer details.")
    public String getInvoiceDetails(String invoiceNumber) {
        log.info("🔧 MCP Tool: getInvoiceDetails({})", invoiceNumber);
        try {
            String json = httpGet(billingBaseUrl + "/api/api/v1/billing/invoices/" + invoiceNumber);
            if (json == null) return "❌ Billing service is unavailable. Please ensure billing-service is running on port 8084.";

            JsonNode root = objectMapper.readTree(json);
            if (!root.has("data") || root.get("data").isNull()) {
                return "❌ Invoice not found: " + invoiceNumber;
            }

            JsonNode data = root.get("data");
            StringBuilder sb = new StringBuilder();
            sb.append("📄 Invoice: ").append(getText(data, "invoiceNumber")).append("\n");
            sb.append("Order: ").append(getText(data, "orderNumber")).append("\n");
            sb.append("Status: ").append(getText(data, "status")).append("\n");
            sb.append("Amount: ₹").append(getNumber(data, "amount")).append("\n");
            sb.append("Paid: ₹").append(getNumber(data, "paidAmount")).append("\n");
            sb.append("Due Date: ").append(getText(data, "dueDate")).append("\n");

            String paidDate = getText(data, "paidDate");
            if (!paidDate.isEmpty()) {
                sb.append("Paid Date: ").append(paidDate).append("\n");
            }
            String description = getText(data, "description");
            if (!description.isEmpty()) {
                sb.append("Description: ").append(description).append("\n");
            }

            sb.append("Account: ").append(getText(data, "accountNumber"))
                    .append(" (").append(getText(data, "customerName")).append(")\n");

            String status = getText(data, "status");
            if ("PENDING".equals(status) || "OVERDUE".equals(status)) {
                sb.append("\n⚠️ This invoice is ").append(status.toLowerCase())
                        .append(" — payment is required.\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Error fetching invoice {}: {}", invoiceNumber, e.getMessage());
            return "❌ Error fetching invoice: " + e.getMessage();
        }
    }

    @Tool("Get a summary of billing status: total outstanding invoices, overdue invoices, and total owed across all accounts.")
    public String getBillingSummary() {
        log.info("🔧 MCP Tool: getBillingSummary()");
        try {
            // Fetch all accounts
            String accountsJson = httpGet(billingBaseUrl + "/api/api/v1/billing/accounts");
            if (accountsJson == null) return "❌ Billing service is unavailable.";

            JsonNode accountsRoot = objectMapper.readTree(accountsJson);
            if (!accountsRoot.has("data") || !accountsRoot.get("data").isArray()) {
                return "❌ No billing data available.";
            }

            int totalAccounts = 0;
            int activeAccounts = 0;
            double totalOutstanding = 0;
            int overdueCount = 0;

            for (JsonNode account : accountsRoot.get("data")) {
                totalAccounts++;
                if ("ACTIVE".equals(getText(account, "status"))) activeAccounts++;

                // Fetch invoices for each account
                String accNum = getText(account, "accountNumber");
                String invJson = httpGet(billingBaseUrl + "/api/api/v1/billing/accounts/" + accNum + "/invoices");
                if (invJson != null) {
                    JsonNode invRoot = objectMapper.readTree(invJson);
                    if (invRoot.has("data") && invRoot.get("data").isArray()) {
                        for (JsonNode inv : invRoot.get("data")) {
                            String invStatus = getText(inv, "status");
                            if ("PENDING".equals(invStatus) || "OVERDUE".equals(invStatus)) {
                                totalOutstanding += Double.parseDouble(getNumber(inv, "amount"));
                                if ("OVERDUE".equals(invStatus)) overdueCount++;
                            }
                        }
                    }
                }
            }

            return String.format("""
                    📊 Billing Summary:
                    • Total Accounts: %d
                    • Active Accounts: %d
                    • Overdue Invoices: %d
                    • Total Outstanding: ₹%.2f
                    """, totalAccounts, activeAccounts, overdueCount, totalOutstanding);
        } catch (Exception e) {
            log.error("Error getting billing summary: {}", e.getMessage());
            return "❌ Error fetching billing summary: " + e.getMessage();
        }
    }

    // ─── HTTP & JSON Helpers ───────────────────────────────────────────────

    private String httpGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
            log.warn("HTTP {} from billing-service: {}", response.statusCode(), url);
            return null;
        } catch (Exception e) {
            log.warn("Failed to call billing-service at {}: {}", url, e.getMessage());
            return null;
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : "";
    }

    private String getNumber(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.toPlainString() : "0";
    }
}
