package com.intellops.copilot.config;

import com.intellops.copilot.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Initializes the pgvector embedding store with runbook and FAQ content
 * on application startup (only if the vector store is empty).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RagService ragService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // Check if vector store already has data
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM intellops_embeddings", Integer.class);
            if (count != null && count > 0) {
                log.info("Vector store already contains {} embeddings, skipping initialization", count);
                return;
            }
        } catch (Exception e) {
            log.info("Vector store table not yet created or query failed: {}", e.getMessage());
        }

        log.info("Seeding pgvector store with knowledge base content...");

        // Index runbooks
        try {
            jdbcTemplate.query("SELECT title, content, 'runbook' AS source FROM runbooks",
                    (rs) -> {
                        String title = rs.getString("title");
                        String content = rs.getString("content");
                        ragService.indexDocument(title, content, "runbook");
                    });
        } catch (Exception e) {
            log.warn("Could not index runbooks: {}", e.getMessage());
        }

        // Index FAQs
        try {
            jdbcTemplate.query("SELECT question, answer FROM faqs",
                    (rs) -> {
                        String question = rs.getString("question");
                        String answer = rs.getString("answer");
                        ragService.indexDocument(question, answer, "faq");
                    });
        } catch (Exception e) {
            log.warn("Could not index FAQs: {}", e.getMessage());
        }

        log.info("Knowledge base seeding complete!");
    }
}
