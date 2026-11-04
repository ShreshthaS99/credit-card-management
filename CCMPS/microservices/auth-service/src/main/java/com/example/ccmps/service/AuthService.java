package com.example.ccmps.service;

import com.example.ccmps.client.CustomerProvisioningClient;
import com.example.ccmps.contract.ProvisionCustomerRequest;
import com.example.ccmps.dto.auth.CurrentUserResponse;
import com.example.ccmps.dto.auth.LoginRequest;
import com.example.ccmps.dto.auth.LoginResponse;
import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.dto.auth.RegisterResponse;
import com.example.ccmps.entity.AppUser;
import com.example.ccmps.enums.UserRole;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.security.TokenService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ccmps.repository.AppUserRepository;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final CustomerProvisioningClient customers;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final DemoProvisioningService demoData;

    public AuthService(AppUserRepository users, CustomerProvisioningClient customers,
                       PasswordEncoder passwords, TokenService tokens,
                       DemoProvisioningService demoData) {
        this.users = users;
        this.customers = customers;
        this.passwords = passwords;
        this.tokens = tokens;
        this.demoData = demoData;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByUsername(normalize(request.username()))
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .filter(u -> passwords.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));
        if (user.getRole() == UserRole.CARDHOLDER) {
            demoData.provision(user.getCustomerId());
        }
        return new LoginResponse(tokens.issue(user.getUsername(), user.getRole().name(),
                user.getCustomerId()), "Bearer", user.getUsername(),
                user.getRole().name(), user.getCustomerId());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (users.existsByUsername(username)) {
            throw new BusinessException("Username already exists.");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Password must not exceed 72 UTF-8 bytes.");
        }
        UUID customerId = UUID.nameUUIDFromBytes(
                ("ccmps:customer:" + username).getBytes(StandardCharsets.UTF_8));
        // Stable provisioning ID allows a retry after an uncertain remote outcome.
        customers.provision(customerId, new ProvisionCustomerRequest(
                request.fullName().trim(), normalize(request.email()), request.phone().trim()),
                tokens.provisioningToken());
        demoData.provision(customerId);
        AppUser user = users.save(AppUser.builder()
                .username(username).password(passwords.encode(request.password()))
                .role(UserRole.CARDHOLDER).customerId(customerId).enabled(true).build());
        return new RegisterResponse(user.getId(), customerId, username, user.getRole().name(),
                "User registered successfully.");
    }

    public CurrentUserResponse getCurrentUser(String username) {
        AppUser user = users.findByUsername(username)
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .orElseThrow(() -> new BadCredentialsException("Account is unavailable."));
        return new CurrentUserResponse(user.getUsername(), user.getRole().name(), user.getCustomerId());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
