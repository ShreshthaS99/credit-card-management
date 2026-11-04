package com.example.ccmps.security;

import com.example.ccmps.entity.AppUser;
import com.example.ccmps.enums.UserRole;
import com.example.ccmps.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminAccountDataLoader
        implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        boolean usernameProvided =
                adminUsername != null
                        && !adminUsername.isBlank();
        boolean passwordProvided =
                adminPassword != null
                        && !adminPassword.isBlank();

        if (!usernameProvided && !passwordProvided) {
            return;
        }

        if (!usernameProvided || !passwordProvided) {
            throw new IllegalStateException(
                    "Set both APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD "
                            + "to provision an administrator."
            );
        }

        String username =
                adminUsername.trim().toLowerCase(Locale.ROOT);

        if (username.length() < 4 || username.length() > 50) {
            throw new IllegalStateException(
                    "The administrator username must be between 4 and 50 characters."
            );
        }

        int passwordByteLength =
                adminPassword.getBytes(StandardCharsets.UTF_8).length;

        if (adminPassword.length() < 12
                || passwordByteLength > 72) {
            throw new IllegalStateException(
                    "The administrator password must contain at least 12 characters "
                            + "and be no more than 72 UTF-8 bytes."
            );
        }

        appUserRepository.findByUsername(username)
                .ifPresentOrElse(
                        existingUser -> {
                            if (existingUser.getRole() != UserRole.ADMIN) {
                                throw new IllegalStateException(
                                        "The configured administrator username "
                                                + "already belongs to a non-admin account."
                                );
                            }
                            System.out.println(
                                    "Configured administrator account already exists."
                            );
                        },
                        () -> createAdmin(username)
                );
    }

    private void createAdmin(String username) {

        AppUser admin = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .customerId(null)
                .enabled(true)
                .build();

        appUserRepository.save(admin);

        System.out.println(
                "Administrator account provisioned: " + username
        );
    }
}
