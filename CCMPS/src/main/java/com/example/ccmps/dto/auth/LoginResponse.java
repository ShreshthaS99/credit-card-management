package com.example.ccmps.dto.auth;

import java.util.UUID;

public record LoginResponse(

        String accessToken,
        String tokenType,
        String username,
        String role,
        UUID customerId

) {
}