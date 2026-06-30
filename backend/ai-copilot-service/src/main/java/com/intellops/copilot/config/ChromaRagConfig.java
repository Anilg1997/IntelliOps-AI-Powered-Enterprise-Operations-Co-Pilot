package com.intellops.copilot.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "atlasops.ai.rag.store", havingValue = "chroma", matchIfMissing = false)
public class ChromaRagConfig {

    @Value("${atlasops.ai.rag.chroma.base-url:http://localhost:8000}")
    private String chromaBaseUrl;

    @Value("${atlasops.ai.rag.chroma.collection:atlasops_embeddings}")
    private String collectionName;

    @Value("${atlasops.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${atlasops.ai.rag.embedding-model:nomic-embed-text}")
    private String embeddingModel;

    @Bean
    @ConditionalOnProperty(name = "atlasops.ai.rag.store", havingValue = "chroma")
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(collectionName)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "atlasops.ai.rag.store", havingValue = "chroma")
    public EmbeddingModel chromaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(embeddingModel)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
