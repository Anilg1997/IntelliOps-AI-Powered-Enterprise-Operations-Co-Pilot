package com.intellops.copilot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Retrieval-Augmented Generation service.
 * <p>
 * Provides two modes:
 * 1. <b>RAG from runbooks/FAQs</b> — retrieves relevant troubleshooting documents
 *    from the pgvector-powered embedding store.
 * 2. <b>Direct database query</b> — fallback to keyword search in runbooks/faqs
 *    tables when vector search is unavailable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<Embedding> embeddingStore;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Maximum number of documents to retrieve for RAG context.
     */
    private static final int TOP_K = 5;

    /**
     * Retrieves relevant context from the vector store by embedding the query
     * and performing similarity search.
     */
    public String retrieveContext(String query) {
        try {
            // 1. Embed the user's question
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 2. Search for relevant segments
            List<Embedding> queryList = List.of(queryEmbedding);
            List<String> relevantSegments = embeddingStore.findRelevant(queryEmbedding, TOP_K)
                    .stream()
                    .map(match -> match.embedded().text())
                    .toList();

            if (relevantSegments.isEmpty()) {
                log.info("No relevant RAG context found for query, using keyword fallback");
                return keywordSearch(query);
            }

            String context = relevantSegments.stream()
                    .collect(joining("\n\n", "--- Relevant Knowledge Base ---\n", "\n--- End Knowledge Base ---"));
            log.info("Retrieved {} relevant documents for RAG", relevantSegments.size());
            return context;
        } catch (Exception e) {
            log.warn("Vector search failed, using keyword fallback: {}", e.getMessage());
            return keywordSearch(query);
        }
    }

    /**
     * Keyword-based fallback search on the runbooks and faqs tables.
     */
    private String keywordSearch(String query) {
        StringBuilder context = new StringBuilder();
        context.append("--- Knowledge Base (Keyword Search) ---\n");

        try {
            // Search runbooks
            String runbookSql = """
                    SELECT title, content FROM runbooks
                    WHERE to_tsvector('english', title || ' ' || content) @@ plainto_tsquery('english', ?)
                    LIMIT 3
                    """;
            jdbcTemplate.query(runbookSql, new Object[]{query}, (rs) -> {
                context.append("📘 Runbook: ").append(rs.getString("title")).append("\n");
                context.append(rs.getString("content")).append("\n\n");
            });

            // Search FAQs
            String faqSql = """
                    SELECT question, answer FROM faqs
                    WHERE to_tsvector('english', question || ' ' || answer) @@ plainto_tsquery('english', ?)
                    LIMIT 3
                    """;
            jdbcTemplate.query(faqSql, new Object[]{query}, (rs) -> {
                context.append("❓ FAQ: ").append(rs.getString("question")).append("\n");
                context.append("Answer: ").append(rs.getString("answer")).append("\n\n");
            });
        } catch (Exception e) {
            log.warn("Keyword search also failed: {}", e.getMessage());
            context.append("(Knowledge base search unavailable)");
        }

        context.append("--- End Knowledge Base ---\n");
        return context.toString();
    }

    /**
     * Indexes a document into the vector store for future RAG retrieval.
     * Used during initialization to seed the knowledge base.
     * Both the stored text and the embedding vector are derived from the same content.
     */
    public void indexDocument(String title, String content, String source) {
        try {
            String text = "Title: " + title + "\n" + content;
            TextSegment segment = TextSegment.from(text);
            dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(text).content();
            embeddingStore.add(embedding, segment);
            log.info("Indexed document: {} ({})", title, source);
        } catch (Exception e) {
            log.warn("Failed to index document '{}': {}", title, e.getMessage());
        }
    }
}
