package com.example.ccmps.service;

import com.example.ccmps.client.CustomerProvisioningClient;
import com.example.ccmps.dto.auth.LoginRequest;
import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.entity.AppUser;
import com.example.ccmps.enums.UserRole;
import com.example.ccmps.repository.AppUserRepository;
import com.example.ccmps.security.TokenService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthDemoProvisioningTest {
    @Mock private AppUserRepository users;
    @Mock private CustomerProvisioningClient customers;
    @Mock private PasswordEncoder passwords;
    @Mock private TokenService tokens;
    @Mock private DemoProvisioningService demoData;
    @InjectMocks private AuthService auth;

    @Test
    void successfulAaravLoginBackfillsDemoData() {
        AppUser user = user(UserRole.CARDHOLDER);
        when(users.findByUsername("aarav")).thenReturn(Optional.of(user));
        when(passwords.matches("test-password", "hash")).thenReturn(true);

        auth.login(new LoginRequest("aarav", "test-password"));

        verify(demoData).provision(user.getCustomerId());
    }

    @Test
    void adminLoginDoesNotSeed() {
        when(users.findByUsername("aarav")).thenReturn(Optional.of(user(UserRole.ADMIN)));
        when(passwords.matches("test-password", "hash")).thenReturn(true);

        auth.login(new LoginRequest("aarav", "test-password"));

        verifyNoInteractions(demoData);
    }

    @Test
    void invalidLoginDoesNotSeed() {
        when(users.findByUsername("aarav")).thenReturn(Optional.of(user(UserRole.CARDHOLDER)));
        assertThrows(BadCredentialsException.class,
                () -> auth.login(new LoginRequest("aarav", "wrong-password")));
        verifyNoInteractions(demoData);
    }

    @Test
    void registrationSeedsBeforeReturningSuccess() {
        when(passwords.encode("test-password")).thenReturn("hash");
        when(tokens.provisioningToken()).thenReturn("Bearer test-system");
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));

        auth.register(new RegisterRequest("new.user", "test-password", "Demo User",
                "new.user@example.test", "9000000000"));

        var order = inOrder(customers, demoData, users);
        order.verify(customers).provision(any(), any(), eq("Bearer test-system"));
        order.verify(demoData).provision(any());
        order.verify(users).save(any());
    }

    private AppUser user(UserRole role) {
        return AppUser.builder().username("aarav").password("hash").enabled(true)
                .role(role).customerId(role == UserRole.ADMIN ? null : UUID.randomUUID()).build();
    }
}
