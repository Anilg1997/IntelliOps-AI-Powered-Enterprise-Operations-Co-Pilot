package com.intellops.copilot.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class BillingTool {

    private final WebClient webClient;

    public BillingTool(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8084").build();
    }

    @Tool("Check billing/invoice status for an order. Use this when a user asks about billing or payment status.")
    public String getInvoiceStatus(String orderNumber) {
        try {
            log.info("AI Tool: Getting invoice status for order: {}", orderNumber);
            Map<?, ?> response = webClient.get()
                    .uri("/api/v1/billing/invoices/order/{orderNumber}", orderNumber)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return "No invoice found for order: " + orderNumber;
            }

            return String.format(
                    "Invoice for order %s: Status=%s, Amount=$%s, Paid=%s",
                    orderNumber,
                    response.get("status"),
                    response.get("totalAmount"),
                    response.get("paymentStatus")
            );
        } catch (Exception e) {
            log.error("Failed to get invoice: {}", e.getMessage());
            return "Error retrieving billing information: " + e.getMessage();
        }
    }

    @Tool("List overdue invoices that need attention.")
    public String getOverdueInvoices() {
        try {
            log.info("AI Tool: Getting overdue invoices");
            Map<?, ?> response = webClient.get()
                    .uri("/api/v1/billing/invoices?status=OVERDUE")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("invoices") == null) {
                return "No overdue invoices found.";
            }

            java.util.List<?> invoices = (java.util.List<?>) response.get("invoices");
            StringBuilder sb = new StringBuilder("Overdue Invoices:\n");
            for (Object inv : invoices) {
                Map<?, ?> invoice = (Map<?, ?>) inv;
                sb.append(String.format("- %s: $%s (Order: %s)\n",
                        invoice.get("invoiceNumber"),
                        invoice.get("totalAmount"),
                        invoice.get("orderNumber")));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to get overdue invoices: {}", e.getMessage());
            return "Error retrieving overdue invoices: " + e.getMessage();
        }
    }
}
