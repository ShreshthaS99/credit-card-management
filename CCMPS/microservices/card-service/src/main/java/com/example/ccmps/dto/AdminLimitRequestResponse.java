package com.example.ccmps.dto;

import com.example.ccmps.entity.LimitRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminLimitRequestResponse(

        UUID id,
        UUID accountId,
        BigDecimal currentLimit,
        BigDecimal requestedLimit,
        String reason,
        String status,
        LocalDateTime requestedAt,
        String reviewedBy,
        LocalDateTime reviewedAt,
        String reviewReason

) {
    public static AdminLimitRequestResponse from(
            LimitRequest request) {

        return new AdminLimitRequestResponse(
                request.getId(),
                request.getAccountId(),
                request.getCurrentLimit(),
                request.getRequestedLimit(),
                request.getReason(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getReviewedBy(),
                request.getReviewedAt(),
                request.getReviewReason()
        );
    }
}
