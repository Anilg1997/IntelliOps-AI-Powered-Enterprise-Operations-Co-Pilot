package com.intellops.order.controller;

import com.intellops.order.dto.ApiResponse;
import com.intellops.order.dto.OrderDto;
import com.intellops.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto.Response>> createOrder(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto.Response response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDto.Response>> getOrder(@PathVariable String orderNumber) {
        OrderDto.Response response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto.Response>>> getAllOrders() {
        List<OrderDto.Response> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderDto.Response>>> getOrdersByCustomer(
            @PathVariable Long customerId) {
        List<OrderDto.Response> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<ApiResponse<OrderDto.Response>> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderDto.StatusUpdateRequest request) {
        OrderDto.Response response = orderService.updateOrderStatus(orderNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));
    }
}
