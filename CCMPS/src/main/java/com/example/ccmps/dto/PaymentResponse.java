package com.example.ccmps.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(

        UUID paymentId,
        String paymentReference,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime createdAt

) {
}