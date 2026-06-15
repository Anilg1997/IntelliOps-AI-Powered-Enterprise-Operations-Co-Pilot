package com.intellops.inventory.repository;

import com.intellops.inventory.document.StockItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends MongoRepository<StockItem, String> {
    Optional<StockItem> findBySku(String sku);
    List<StockItem> findByStatus(StockItem.StockStatus status);
    List<StockItem> findByTotalQuantityLessThanEqual(int threshold);
}
