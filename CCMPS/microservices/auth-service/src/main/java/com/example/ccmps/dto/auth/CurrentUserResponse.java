package com.example.ccmps.dto.auth;

import java.util.UUID;

public record CurrentUserResponse(

        String username,
       String role,
        UUID customerId

) {
}