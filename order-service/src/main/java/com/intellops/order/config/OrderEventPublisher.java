package com.intellops.order.config;

import com.intellops.order.entity.Order;
import com.intellops.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        var event = Map.of(
                "eventType", "ORDER_CREATED",
                "orderNumber", order.getOrderNumber(),
                "customerId", order.getCustomer().getId(),
                "customerEmail", order.getCustomer().getEmail(),
                "totalAmount", order.getTotalAmount(),
                "status", order.getStatus().name(),
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, order.getOrderNumber(), event);
        log.info("Published order.created event for order: {}", order.getOrderNumber());
    }

    public void publishOrderStatusChanged(Order order, OrderStatus oldStatus) {
        var event = Map.of(
                "eventType", "ORDER_STATUS_CHANGED",
                "orderNumber", order.getOrderNumber(),
                "customerId", order.getCustomer().getId(),
                "oldStatus", oldStatus.name(),
                "newStatus", order.getStatus().name(),
                "reason", order.getStatusReason(),
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.ORDER_STATUS_CHANGED_TOPIC, order.getOrderNumber(), event);
        log.info("Published order.status.changed for order: {} ({} -> {})",
                order.getOrderNumber(), oldStatus, order.getStatus());
    }

    public void publishPaymentFailed(Order order) {
        var event = Map.of(
                "eventType", "PAYMENT_FAILED",
                "orderNumber", order.getOrderNumber(),
                "customerId", order.getCustomer().getId(),
                "customerEmail", order.getCustomer().getEmail(),
                "totalAmount", order.getTotalAmount(),
                "reason", order.getStatusReason(),
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.PAYMENT_FAILED_TOPIC, order.getOrderNumber(), event);
        log.warn("Published payment.failed event for order: {}", order.getOrderNumber());
    }
}
