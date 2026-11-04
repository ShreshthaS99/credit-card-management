package com.example.ccmps.contract;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProvisionCustomerRequest(@NotBlank String fullName,
                                       @NotBlank @Email String email,
                                       @NotBlank String phone) {
}
