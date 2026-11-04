package com.example.ccmps.security;

import com.example.ccmps.dto.auth.RegisterRequest;
import com.example.ccmps.enums.UserRole;
import com.example.ccmps.repository.AppUserRepository;
import com.example.ccmps.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local-demo")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoAccountDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoAccountDataLoader.class);
    private static final String USERNAME = "aarav";
    private final AppUserRepository users;
    private final AuthService auth;
    private final PasswordEncoder passwords;
    private final String password;

    public DemoAccountDataLoader(AppUserRepository users, AuthService auth,
                                PasswordEncoder passwords,
                                @Value("${app.demo.password:}") String password) {
        this.users = users;
        this.auth = auth;
        this.passwords = passwords;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (password.isBlank() || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Set APP_DEMO_PASSWORD to at least 12 characters and at most 72 UTF-8 bytes "
                            + "when using the local-demo profile.");
        }
        var existing = users.findByUsername(USERNAME);
        if (existing.isPresent()) {
            var user = existing.get();
            UUID customerId = UUID.nameUUIDFromBytes(
                    ("ccmps:customer:" + USERNAME).getBytes(StandardCharsets.UTF_8));
            if (user.getRole() != UserRole.CARDHOLDER
                    || !Boolean.TRUE.equals(user.getEnabled())
                    || !customerId.equals(user.getCustomerId())
                    || !passwords.matches(password, user.getPassword())) {
                throw new IllegalStateException(
                        "Existing aarav account conflicts with the local-demo configuration. "
                                + "Existing accounts are not overwritten; use its original password "
                                + "and customer mapping, or disable the local-demo profile.");
            }
            log.info("Local demo account already exists; credentials were not changed.");
            return;
        }
        auth.register(new RegisterRequest(USERNAME, password, "Aarav Demo",
                "aarav@example.test", "9000000000"));
        log.info("Local demo account aarav provisioned. Sign in with APP_DEMO_PASSWORD.");
    }
}
