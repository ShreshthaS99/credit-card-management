package com.example.ccmps.controller;

import com.example.ccmps.dto.UpdateCustomerRequest;
import com.example.ccmps.entity.Customer;
import com.example.ccmps.service.CustomerService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{customerId}")
    public Customer getCustomer(
            @PathVariable UUID customerId) {

        return customerService
                .getCustomer(customerId);
    }

    @PutMapping("/{customerId}")
    public Customer updateCustomer(
            @PathVariable UUID customerId,
            @Valid
            @RequestBody
            UpdateCustomerRequest request) {

        return customerService
                .updateCustomer(
                        customerId,
                        request
                );
    }
}