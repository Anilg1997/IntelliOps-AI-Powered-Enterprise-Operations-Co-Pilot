package com.intellops.order.repository;

import com.intellops.order.entity.Order;
import com.intellops.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.lineItems li JOIN FETCH li.product WHERE o.orderNumber = :orderNumber")
    Optional<Order> findFullOrderByOrderNumber(@Param("orderNumber") String orderNumber);

    @Query("SELECT o FROM Order o JOIN FETCH o.customer LEFT JOIN FETCH o.lineItems WHERE o.id = :id")
    Optional<Order> findFullOrderById(@Param("id") Long id);
}
