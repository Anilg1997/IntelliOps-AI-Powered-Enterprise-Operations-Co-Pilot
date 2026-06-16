package com.intellops.inventory.repository;

import com.intellops.inventory.model.StockReservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends MongoRepository<StockReservation, String> {

    Optional<StockReservation> findByOrderIdAndProductId(String orderId, String productId);

    List<StockReservation> findByOrderId(String orderId);

    List<StockReservation> findByProductIdAndStatus(String productId, String status);

    Optional<StockReservation> findByReservationId(String reservationId);
}
