package com.intellops.order.service;

import com.intellops.order.dto.CustomerDto;
import com.intellops.order.dto.ProductDto;
import com.intellops.order.entity.Customer;
import com.intellops.order.entity.Product;
import com.intellops.order.exception.DuplicateResourceException;
import com.intellops.order.exception.ResourceNotFoundException;
import com.intellops.order.repository.CustomerRepository;
import com.intellops.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public CustomerDto.Response createCustomer(CustomerDto.Request request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer already exists with email: " + request.getEmail());
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();
        customer = customerRepository.save(customer);
        log.info("Created customer: {} ({})", customer.getName(), customer.getEmail());
        return toResponse(customer);
    }

    public CustomerDto.Response getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toResponse(customer);
    }

    public CustomerDto.Response getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return toResponse(customer);
    }

    public List<CustomerDto.Response> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Customer getCustomerEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public Customer getCustomerEntityByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private CustomerDto.Response toResponse(Customer c) {
        return CustomerDto.Response.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().format(DTF) : null)
                .updatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().format(DTF) : null)
                .build();
    }
}
