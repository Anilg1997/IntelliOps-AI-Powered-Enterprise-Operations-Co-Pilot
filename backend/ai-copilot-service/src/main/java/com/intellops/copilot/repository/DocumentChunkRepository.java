package com.intellops.copilot.repository;

import com.intellops.copilot.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Query(value = "SELECT * FROM document_chunks ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(@Param("embedding") String embedding, @Param("limit") int limit);

    List<DocumentChunk> findByDocumentName(String documentName);
}
