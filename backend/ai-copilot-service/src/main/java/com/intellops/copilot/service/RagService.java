package com.intellops.copilot.service;

import com.intellops.copilot.model.DocumentChunk;
import com.intellops.copilot.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final DocumentChunkRepository documentChunkRepository;

    @Value("${intellops.ai.rag.enabled:true}")
    private boolean ragEnabled;

    public String retrieveRelevantContext(String query) {
        if (!ragEnabled) {
            return getDefaultContext();
        }

        try {
            // Simple text search fallback when pgvector embedding is not available
            List<DocumentChunk> chunks = documentChunkRepository.findAll();

            if (chunks.isEmpty()) {
                return getDefaultContext();
            }

            // Simple relevance scoring based on keyword matching
            List<String> queryWords = List.of(query.toLowerCase().split("\\s+"));

            List<String> relevantChunks = chunks.stream()
                    .filter(chunk -> {
                        String text = chunk.getChunkText().toLowerCase();
                        return queryWords.stream().anyMatch(text::contains);
                    })
                    .limit(5)
                    .map(DocumentChunk::getChunkText)
                    .collect(Collectors.toList());

            if (relevantChunks.isEmpty()) {
                return getDefaultContext();
            }

            return "Relevant documentation:\n" + String.join("\n---\n", relevantChunks);
        } catch (Exception e) {
            log.warn("RAG retrieval failed, using default context: {}", e.getMessage());
            return getDefaultContext();
        }
    }

    private String getDefaultContext() {
        return """
                Enterprise Operations Runbook:
                
                Order Status Codes:
                - PENDING: Order received, awaiting confirmation
                - CONFIRMED: Order confirmed, payment verified
                - PROCESSING: Order is being prepared/fulfilled
                - SHIPPED: Order has been shipped to customer
                - DELIVERED: Order delivered successfully
                - CANCELLED: Order was cancelled
                
                Common Issues:
                - Stock Hold: Order held due to insufficient inventory
                - Payment Pending: Awaiting payment confirmation
                - Shipping Delay: Logistics delay, check with carrier
                
                Escalation Procedures:
                - Orders stuck > 24 hours: Escalate to operations lead
                - Payment disputes: Route to finance team
                - Inventory discrepancies: Check with warehouse manager
                """;
    }
}
