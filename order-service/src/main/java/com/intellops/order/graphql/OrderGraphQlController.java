package com.intellops.order.graphql;

import com.intellops.order.dto.OrderDto;
import com.intellops.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderGraphQlController {

    private final OrderService orderService;

    @QueryMapping
    public OrderDto.Response order(@Argument String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }

    @QueryMapping
    public List<OrderDto.Response> orders(@Argument Long customerId) {
        if (customerId != null) {
            return orderService.getOrdersByCustomer(customerId);
        }
        return orderService.getAllOrders();
    }

    @QueryMapping
    public List<OrderDto.Response> allOrders() {
        return orderService.getAllOrders();
    }
}
