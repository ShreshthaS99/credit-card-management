package com.example.ccmps.dto;
import jakarta.validation.constraints.NotBlank;

public record FraudResponseRequest(

        @NotBlank(message = "Response is required")
        String response

) {
}