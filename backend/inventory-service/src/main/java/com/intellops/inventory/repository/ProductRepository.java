package com.intellops.inventory.repository;

import com.intellops.inventory.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryAndActiveTrue(String category);

    List<Product> findByActiveTrue();

    boolean existsBySku(String sku);
}
