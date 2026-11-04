package com.example.ccmps.controller;

import com.example.ccmps.contract.ProvisionCustomerRequest;
import com.example.ccmps.entity.Customer;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.repository.CustomerRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerProvisioningController {
    private final CustomerRepository customers;

    public CustomerProvisioningController(CustomerRepository customers) {
        this.customers = customers;
    }

    @PutMapping("/internal/customers/{customerId}")
    @Transactional
    public void provision(@PathVariable UUID customerId,
                          @Valid @RequestBody ProvisionCustomerRequest request) {
        Customer existing = customers.findById(customerId).orElse(null);
        if (existing != null) {
            if (!existing.getEmail().equals(request.email())
                    || !existing.getFullName().equals(request.fullName())
                    || !existing.getPhone().equals(request.phone())) {
                throw new BusinessException("Customer provisioning details conflict.");
            }
            return;
        }
        if (customers.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Email address is already registered.");
        }
        customers.save(Customer.builder().id(customerId)
                .fullName(request.fullName()).email(request.email())
                .phone(request.phone()).status("ACTIVE").build());
    }

    @GetMapping("/api/admin/metrics/customers")
    public long count() {
        return customers.count();
    }
}
