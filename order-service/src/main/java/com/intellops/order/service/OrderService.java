package com.intellops.order.service;

import com.intellops.order.config.OrderEventPublisher;
import com.intellops.order.dto.OrderDto;
import com.intellops.order.entity.*;
import com.intellops.order.exception.ResourceNotFoundException;
import com.intellops.order.grpc.StockServiceClient;
import com.intellops.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderEventPublisher eventPublisher;
    private final StockServiceClient stockServiceClient;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ORDER_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        Customer customer = customerService.getCustomerEntityByEmail(request.getCustomerEmail());

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .customer(customer)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        boolean stockCheckFailed = false;
        StringBuilder stockWarnings = new StringBuilder();

        for (OrderDto.LineItemRequest itemReq : request.getLineItems()) {
            Product product = productService.getProductEntityBySku(itemReq.getProductSku());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            // Check stock availability via gRPC (Inventory Service)
            StockServiceClient.StockCheckResult stockResult =
                    stockServiceClient.checkStock(itemReq.getProductSku(), itemReq.getQuantity());

            if (!stockResult.isAvailable()) {
                stockCheckFailed = true;
                String warning = String.format("Insufficient stock for '%s' (SKU: %s): requested %d, available %d. ETA: %s",
                        product.getName(), itemReq.getProductSku(), itemReq.getQuantity(),
                        stockResult.getAvailableQuantity(),
                        stockResult.getEstimatedRestockDate().isEmpty() ? "unknown" : stockResult.getEstimatedRestockDate());
                stockWarnings.append(warning).append("; ");
                log.warn("{}", warning);
            } else {
                // Reserve stock in inventory
                stockServiceClient.reserveStock(itemReq.getProductSku(), itemReq.getQuantity(), order.getOrderNumber());
            }

            OrderLineItem lineItem = OrderLineItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            order.getLineItems().add(lineItem);
        }

        // If stock check failed, put order on hold with reason
        if (stockCheckFailed) {
            order.setStatus(OrderStatus.ON_HOLD);
            order.setStatusReason("STOCK_HOLD: " + stockWarnings.toString());
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);
        log.info("Created order: {} for customer: {} (total: ₹{}, status: {})",
                order.getOrderNumber(), customer.getEmail(), total, order.getStatus());

        // Publish Kafka event for the notification/activity service
        eventPublisher.publishOrderCreated(order);

        if (stockCheckFailed) {
            eventPublisher.publishOrderStatusChanged(order, OrderStatus.PENDING);
        }

        return toFullResponse(order);
    }

    public OrderDto.Response getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findFullOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return toFullResponse(order);
    }

    public List<OrderDto.Response> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toBasicResponse)
                .toList();
    }

    public List<OrderDto.Response> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toBasicResponse)
                .toList();
    }

    @Transactional
    public OrderDto.Response updateOrderStatus(String orderNumber, OrderDto.StatusUpdateRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getNewStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getNewStatus()
                    + ". Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, ON_HOLD, CANCELLED, REFUNDED");
        }

        order.setStatus(newStatus);
        order.setStatusReason(request.getReason());
        order = orderRepository.save(order);
        log.info("Order {} status updated: {} -> {} (reason: {})",
                orderNumber, oldStatus, newStatus, request.getReason());

        // Publish status change event
        eventPublisher.publishOrderStatusChanged(order, oldStatus);

        // If the status change indicates a payment failure, publish that event too
        if (newStatus == OrderStatus.ON_HOLD && "PAYMENT_FAILED".equalsIgnoreCase(request.getReason())) {
            eventPublisher.publishPaymentFailed(order);
        }

        return toFullResponse(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(ORDER_DATE_FMT)
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private OrderDto.Response toFullResponse(Order order) {
        OrderDto.Response response = toBasicResponse(order);
        response.setLineItems(order.getLineItems().stream()
                .map(this::toLineItemResponse)
                .toList());
        return response;
    }

    private OrderDto.Response toBasicResponse(Order order) {
        return OrderDto.Response.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .statusReason(order.getStatusReason())
                .totalAmount(order.getTotalAmount())
                .customer(customerService.getCustomer(order.getCustomer().getId()))
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().format(DTF) : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().format(DTF) : null)
                .build();
    }

    private OrderDto.LineItemResponse toLineItemResponse(OrderLineItem item) {
        return OrderDto.LineItemResponse.builder()
                .id(item.getId())
                .product(productService.getProduct(item.getProduct().getId()))
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
