package com.example.ccmps.security;

import com.example.ccmps.client.CustomerProvisioningClient;
import com.example.ccmps.dto.auth.LoginRequest;
import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.entity.AppUser;
import com.example.ccmps.enums.UserRole;
import com.example.ccmps.repository.AppUserRepository;
import com.example.ccmps.service.AuthService;
import com.example.ccmps.service.DemoProvisioningService;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoAccountDataLoaderTest {
    private static final String PASSWORD = "local-test-password";
    @Mock private AppUserRepository users;
    @Mock private AuthService auth;
    @Mock private PasswordEncoder passwords;

    @Test
    void createsDemoThroughExistingRegistrationFlow() {
        when(users.findByUsername("aarav")).thenReturn(Optional.empty());

        loader(PASSWORD).run();

        verify(auth).register(new RegisterRequest("aarav", PASSWORD,
                "Aarav Demo", "aarav@example.test", "9000000000"));
        verify(users, never()).save(any());
    }

    @Test
    void automaticallyCreatedAccountCanLoginWithConfiguredPassword() {
        var customers = mock(CustomerProvisioningClient.class);
        var tokens = mock(TokenService.class);
        var demo = mock(DemoProvisioningService.class);
        var encoder = new BCryptPasswordEncoder();
        var service = new AuthService(users, customers, encoder, tokens, demo);
        var saved = new AtomicReference<AppUser>();
        when(users.findByUsername("aarav"))
                .thenAnswer(call -> Optional.ofNullable(saved.get()));
        when(users.save(any())).thenAnswer(call -> {
            AppUser user = call.getArgument(0);
            saved.set(user);
            return user;
        });

        new DemoAccountDataLoader(users, service, encoder, PASSWORD).run();
        var response = service.login(new LoginRequest("aarav", PASSWORD));

        assertEquals("aarav", response.username());
        assertEquals("CARDHOLDER", response.role());
        assertEquals(saved.get().getCustomerId(), response.customerId());
        assertNotEquals(PASSWORD, saved.get().getPassword());
        verify(customers).provision(eq(saved.get().getCustomerId()), any(), any());
        verify(demo, times(2)).provision(saved.get().getCustomerId());
        verify(tokens).issue("aarav", "CARDHOLDER", saved.get().getCustomerId());
        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("aarav", "wrong-password")));
    }

    @Test
    void repeatedStartupPreservesExistingAccount() {
        when(users.findByUsername("aarav")).thenReturn(Optional.of(existing()));
        when(passwords.matches(PASSWORD, "hash")).thenReturn(true);

        loader(PASSWORD).run();
        loader(PASSWORD).run();

        verifyNoInteractions(auth);
        verify(users, never()).save(any());
    }

    @Test
    void conflictingPasswordDoesNotOverwriteAccount() {
        when(users.findByUsername("aarav")).thenReturn(Optional.of(existing()));

        assertThrows(IllegalStateException.class, () -> loader(PASSWORD).run());

        verifyNoInteractions(auth);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsAdminDisabledAndUnrelatedAccounts() {
        AppUser admin = existing();
        admin.setRole(UserRole.ADMIN);
        AppUser disabled = existing();
        disabled.setEnabled(false);
        AppUser unrelated = existing();
        unrelated.setCustomerId(UUID.randomUUID());
        for (AppUser user : new AppUser[]{admin, disabled, unrelated}) {
            when(users.findByUsername("aarav")).thenReturn(Optional.of(user));
            assertThrows(IllegalStateException.class, () -> loader(PASSWORD).run());
        }
        verifyNoInteractions(auth, passwords);
    }

    @Test
    void rejectsMissingShortAndOversizedPasswords() {
        for (String value : new String[]{"", "short", "x".repeat(73), "\u00e9".repeat(37)}) {
            assertThrows(IllegalStateException.class, () -> loader(value).run());
        }
        verifyNoInteractions(users, auth, passwords);
    }

    @Test
    void provisioningFailureIsNotHidden() {
        when(users.findByUsername("aarav")).thenReturn(Optional.empty());
        when(auth.register(any())).thenThrow(new IllegalStateException("dependency unavailable"));

        assertThrows(IllegalStateException.class, () -> loader(PASSWORD).run());
    }

    @Test
    void bootstrapRequiresExplicitProfileAndEnabledDemo() {
        context().run(c -> assertTrue(c.getBeansOfType(DemoAccountDataLoader.class).isEmpty()));
        context().withPropertyValues("spring.profiles.active=local-demo", "app.demo.enabled=false")
                .run(c -> assertTrue(c.getBeansOfType(DemoAccountDataLoader.class).isEmpty()));
        context().withPropertyValues("spring.profiles.active=local-demo")
                .run(c -> assertEquals(1, c.getBeansOfType(DemoAccountDataLoader.class).size()));
    }

    private ApplicationContextRunner context() {
        return new ApplicationContextRunner()
                .withUserConfiguration(DemoAccountDataLoader.class)
                .withBean(AppUserRepository.class, () -> users)
                .withBean(AuthService.class, () -> auth)
                .withBean(PasswordEncoder.class, () -> passwords);
    }

    private DemoAccountDataLoader loader(String password) {
        return new DemoAccountDataLoader(users, auth, passwords, password);
    }

    private AppUser existing() {
        return AppUser.builder().username("aarav").password("hash")
                .role(UserRole.CARDHOLDER).enabled(true)
                .customerId(UUID.nameUUIDFromBytes(
                        "ccmps:customer:aarav".getBytes(StandardCharsets.UTF_8))).build();
    }
}
