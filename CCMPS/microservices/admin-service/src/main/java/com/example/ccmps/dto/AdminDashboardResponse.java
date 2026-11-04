package com.example.ccmps.dto;

public record AdminDashboardResponse(

        long customerCount,
        long accountCount,
        long cardCount,
        long pendingLimitRequestCount,
        long openFraudAlertCount

) {
}
