package com.example.ccmps.security;

import com.example.ccmps.entity.AppUser;
import com.example.ccmps.entity.Customer;

import com.example.ccmps.enums.UserRole;

import com.example.ccmps.repository.AppUserRepository;
import com.example.ccmps.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityDataLoader
        implements CommandLineRunner {

    private static final String DEMO_EMAIL =
            "aarav.demo@example.com";

    private static final String DEMO_USERNAME =
            "aarav";

    private static final String DEMO_PASSWORD =
            "Cardwise@123";

    private final CustomerRepository
            customerRepository;

    private final AppUserRepository
            appUserRepository;

    private final PasswordEncoder
            passwordEncoder;

    @Override
    public void run(
            String... args) {

        Customer customer =
                customerRepository
                        .findByEmail(
                                DEMO_EMAIL
                        )
                        .orElseGet(
                                this::createDemoCustomer
                        );

        if (!appUserRepository
                .existsByUsername(
                        DEMO_USERNAME
                )) {

            AppUser demoUser =
                    AppUser.builder()
                            .username(
                                    DEMO_USERNAME
                            )
                            .password(
                                    passwordEncoder.encode(
                                            DEMO_PASSWORD
                                    )
                            )
                            .role(
                                    UserRole.CARDHOLDER
                            )
                            .customerId(
                                    customer.getId()
                            )
                            .enabled(true)
                            .build();

            appUserRepository.save(
                    demoUser
            );

            System.out.println(
                    "Demo security user created: aarav"
            );

        } else {

            System.out.println(
                    "Demo security user already exists: aarav"
            );
        }
    }

    private Customer createDemoCustomer() {

        Customer customer =
                Customer.builder()
                        .fullName(
                                "Aarav Sharma"
                        )
                        .email(
                                DEMO_EMAIL
                        )
                        .phone(
                                "9000000000"
                        )
                        .status(
                                "ACTIVE"
                        )
                        .build();

        return customerRepository.save(
                customer
        );
    }
}