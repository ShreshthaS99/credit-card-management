package com.example.ccmps.dto.auth;

import java.util.UUID;

public record RegisterResponse(

        UUID userId,
        UUID customerId,
        String username,
        String role,
        String message

) {
}