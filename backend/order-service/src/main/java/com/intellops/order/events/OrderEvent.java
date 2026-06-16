package com.intellops.order.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private String eventType;
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String status;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;

    public static OrderEvent of(String eventType, String orderNumber, String status) {
        return OrderEvent.builder()
                .eventType(eventType)
                .orderNumber(orderNumber)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
