package com.example.ccmps.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(

        @NotBlank(
                message = "Full name is required"
        )
        String fullName,

        @NotBlank(
                message = "Email is required"
        )
        @Email(
                message = "Enter a valid email address"
        )
        String email,

        @NotBlank(
                message = "Phone number is required"
        )
        String phone

) {
}