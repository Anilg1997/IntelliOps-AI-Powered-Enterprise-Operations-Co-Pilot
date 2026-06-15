package com.intellops.inventory.repository;

import com.intellops.inventory.document.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCatalogRepository extends MongoRepository<ProductDocument, String> {
    Optional<ProductDocument> findBySku(String sku);
    boolean existsBySku(String sku);
    List<ProductDocument> findByCategory(String category);
    List<ProductDocument> findByActiveTrue();
    List<ProductDocument> findByTagsContaining(String tag);
}
