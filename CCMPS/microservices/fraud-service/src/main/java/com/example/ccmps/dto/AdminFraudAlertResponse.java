package com.example.ccmps.dto;

import com.example.ccmps.entity.FraudAlert;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminFraudAlertResponse(

        UUID id,
        UUID accountId,
        UUID transactionId,
        String riskReasonCode,
        String status,
        String customerResponse,
        String caseReference,
        LocalDateTime createdAt,
        LocalDateTime respondedAt,
        String analystDisposition,
        String analystNotes,
        String reviewedBy,
        LocalDateTime reviewedAt

) {
    public static AdminFraudAlertResponse from(
            FraudAlert alert) {

        return new AdminFraudAlertResponse(
                alert.getId(),
                alert.getAccountId(),
                alert.getTransactionId(),
                alert.getRiskReasonCode(),
                alert.getStatus(),
                alert.getCustomerResponse(),
                alert.getCaseReference(),
                alert.getCreatedAt(),
                alert.getRespondedAt(),
                alert.getAnalystDisposition(),
                alert.getAnalystNotes(),
                alert.getReviewedBy(),
                alert.getReviewedAt()
        );
    }
}
