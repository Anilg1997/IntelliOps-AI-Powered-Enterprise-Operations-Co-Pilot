package com.intellops.copilot.service;

import com.intellops.copilot.model.DocumentChunk;
import com.intellops.copilot.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private DocumentChunkRepository repository;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(repository);
        ReflectionTestUtils.setField(ragService, "ragEnabled", true);
    }

    @Test
    void retrieveRelevantContext_whenDisabled_shouldReturnDefault() {
        ReflectionTestUtils.setField(ragService, "ragEnabled", false);

        String context = ragService.retrieveRelevantContext("test query");

        assertThat(context).contains("Enterprise Operations Runbook");
    }

    @Test
    void retrieveRelevantContext_whenNoChunks_shouldReturnDefault() {
        when(repository.findAll()).thenReturn(List.of());

        String context = ragService.retrieveRelevantContext("order status");

        assertThat(context).contains("Order Status Codes");
        assertThat(context).contains("PENDING");
    }

    @Test
    void retrieveRelevantContext_withMatchingChunks_shouldReturnFiltered() {
        DocumentChunk chunk = DocumentChunk.builder()
                .chunkText("Orders in PENDING status need confirmation.")
                .build();
        when(repository.findAll()).thenReturn(List.of(chunk));

        String context = ragService.retrieveRelevantContext("pending orders");

        assertThat(context).contains("Orders in PENDING status");
    }

    @Test
    void retrieveRelevantContext_withNoMatch_shouldReturnDefault() {
        DocumentChunk chunk = DocumentChunk.builder()
                .chunkText("Billing invoices are generated monthly.")
                .build();
        when(repository.findAll()).thenReturn(List.of(chunk));

        String context = ragService.retrieveRelevantContext("shipping delays");

        assertThat(context).contains("Enterprise Operations Runbook");
    }
}
