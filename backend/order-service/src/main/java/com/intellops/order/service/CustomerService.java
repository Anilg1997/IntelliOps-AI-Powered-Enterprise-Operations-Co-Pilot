package com.intellops.order.service;

import com.intellops.order.dto.CreateCustomerRequest;
import com.intellops.order.dto.OrderResponse;
import com.intellops.order.entity.Customer;
import com.intellops.order.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponse.CustomerDto createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .customerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created: {}", customer.getCustomerNumber());

        return toDto(customer);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse.CustomerDto> listCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse.CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return toDto(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public OrderResponse.CustomerDto toDto(Customer customer) {
        return OrderResponse.CustomerDto.builder()
                .id(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();
    }
}
