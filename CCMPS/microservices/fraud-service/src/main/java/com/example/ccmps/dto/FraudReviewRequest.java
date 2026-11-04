package com.example.ccmps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FraudReviewRequest(

        @NotNull(message = "Disposition is required")
        FraudDisposition disposition,

        @NotBlank(message = "Review notes are required")
        @Size(max = 1000, message = "Review notes must not exceed 1000 characters")
        String notes

) {
}
