package com.intellops.copilot.config;

import com.intellops.copilot.service.RagService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * RAG configuration — embedding model (Ollama) + pgvector embedding store.
 */
@Configuration
public class RagConfig {

    @Value("${intellops.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${intellops.ai.rag.embedding-model:llama3.1}")
    private String embeddingModel;

    @Value("${intellops.ai.rag.embedding-dimensions:768}")
    private Integer embeddingDimensions;

    @Value("${intellops.ai.rag.table:intellops_embeddings}")
    private String vectorTable;

    /**
     * Embedding model backed by Ollama.
     * llama3.1 supports embeddings natively with 768-dimension vectors.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(embeddingModel)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * pgvector-based embedding store stored in PostgreSQL.
     * Table and vector dimensions are configurable via properties.
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table(vectorTable)
                .dimension(embeddingDimensions)
                .useIndex(true)
                .indexListSize(100)  // HNSW index for efficient search
                .build();
    }
}
