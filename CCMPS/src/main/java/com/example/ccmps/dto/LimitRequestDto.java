package com.example.ccmps.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LimitRequestDto(

        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Requested limit is required")
        @DecimalMin(value = "1.00", message = "Limit must be greater than zero")
        BigDecimal requestedLimit,

        @NotBlank(message = "Reason is required")
        String reason

) {
}