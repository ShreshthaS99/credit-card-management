package com.example.ccmps.controller;

import com.example.ccmps.dto.auth.CurrentUserResponse;
import com.example.ccmps.dto.auth.LoginRequest;
import com.example.ccmps.dto.auth.LoginResponse;
import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.dto.auth.RegisterResponse;

import com.example.ccmps.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request) {

        return authService.login(
                request
        );
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        return authService.register(
                request
        );
    }

    @GetMapping("/me")
    public CurrentUserResponse
    getCurrentUser(
            Authentication authentication) {

        return authService
                .getCurrentUser(
                        authentication.getName()
                );
    }
}