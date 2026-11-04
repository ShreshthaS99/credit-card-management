package com.example.ccmps.service;

import com.example.ccmps.dto.UpdateCustomerRequest;
import com.example.ccmps.entity.Customer;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Customer getCustomer(UUID customerId) {

        return customerRepository
                .findById(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Customer not found: "
                                        + customerId
                        )
                );
    }

    @Transactional
    public Customer updateCustomer(
            UUID customerId,
            UpdateCustomerRequest request) {

        Customer customer =
                getCustomer(customerId);

        customer.setFullName(
                request.fullName().trim()
        );

        customer.setEmail(
                request.email()
                        .trim()
                        .toLowerCase()
        );

        customer.setPhone(
                request.phone().trim()
        );

        return customerRepository.save(
                customer
        );
    }
}