package com.example.ccmps.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RewardRedemptionRequest(

        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Points are required")
        @Min(value = 1, message = "Points must be greater than zero")
        Integer points

) {
}