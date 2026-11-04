package com.example.ccmps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LimitReviewRequest(

        @NotNull(message = "Decision is required")
        LimitDecision decision,

        @NotBlank(message = "Review reason is required")
        @Size(max = 500, message = "Review reason must not exceed 500 characters")
        String reason

) {
}
