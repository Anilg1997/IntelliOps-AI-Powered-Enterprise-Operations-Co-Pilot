package com.intellops.order.controller;

import com.intellops.order.dto.ApiResponse;
import com.intellops.order.dto.CustomerDto;
import com.intellops.order.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto.Response>> createCustomer(
            @Valid @RequestBody CustomerDto.Request request) {
        CustomerDto.Response response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Customer created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> getCustomer(@PathVariable Long id) {
        CustomerDto.Response response = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto.Response>>> getAllCustomers() {
        List<CustomerDto.Response> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<CustomerDto.Response>> getCustomerByEmail(@PathVariable String email) {
        CustomerDto.Response response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
