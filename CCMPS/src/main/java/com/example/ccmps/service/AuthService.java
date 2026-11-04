package com.example.ccmps.service;

import com.example.ccmps.dto.auth.CurrentUserResponse;
import com.example.ccmps.dto.auth.LoginRequest;
import com.example.ccmps.dto.auth.LoginResponse;
import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.dto.auth.RegisterResponse;

import com.example.ccmps.entity.AppUser;
import com.example.ccmps.entity.Customer;

import com.example.ccmps.enums.UserRole;

import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;

import com.example.ccmps.repository.AppUserRepository;
import com.example.ccmps.repository.CustomerRepository;

import com.example.ccmps.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final AppUserRepository appUserRepository;

    private final CustomerRepository customerRepository;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(
            LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails)
                        authentication.getPrincipal();

        AppUser appUser =
                findUserByUsername(
                        request.username()
                );

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                appUser.getRole().name()
        );

        if (appUser.getCustomerId() != null) {

            claims.put(
                    "customerId",
                    appUser.getCustomerId().toString()
            );
        }

        String accessToken =
                jwtService.generateToken(
                        userDetails,
                        claims
                );

        return new LoginResponse(
                accessToken,
                "Bearer",
                appUser.getUsername(),
                appUser.getRole().name(),
                appUser.getCustomerId()
        );
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request) {

        String username =
                request.username()
                        .trim()
                        .toLowerCase();

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        if (appUserRepository
                .existsByUsername(username)) {

            throw new BusinessException(
                    "Username already exists."
            );
        }

        if (customerRepository
                .findByEmail(email)
                .isPresent()) {

            throw new BusinessException(
                    "Email address is already registered."
            );
        }

        Customer customer =
                Customer.builder()
                        .fullName(
                                request.fullName().trim()
                        )
                        .email(email)
                        .phone(
                                request.phone().trim()
                        )
                        .status("ACTIVE")
                        .build();

        Customer savedCustomer =
                customerRepository.save(
                        customer
                );

        AppUser appUser =
                AppUser.builder()
                        .username(username)
                        .password(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .role(
                                UserRole.CARDHOLDER
                        )
                        .customerId(
                                savedCustomer.getId()
                        )
                        .enabled(true)
                        .build();

        AppUser savedUser =
                appUserRepository.save(
                        appUser
                );

        return new RegisterResponse(
                savedUser.getId(),
                savedCustomer.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name(),
                "User registered successfully."
        );
    }

    public CurrentUserResponse getCurrentUser(
            String username) {

        AppUser appUser =
                findUserByUsername(
                        username
                );

        return new CurrentUserResponse(
                appUser.getUsername(),
                appUser.getRole().name(),
                appUser.getCustomerId()
        );
    }

    private AppUser findUserByUsername(
            String username) {

        return appUserRepository
                .findByUsername(username)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Application user not found: "
                                                + username
                                )
                );
    }
}