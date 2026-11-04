package com.example.ccmps.controller;

import com.example.ccmps.client.CardMetricsClient;
import com.example.ccmps.client.CustomerMetricsClient;
import com.example.ccmps.client.FraudMetricsClient;
import com.example.ccmps.dto.AdminDashboardResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final CustomerMetricsClient customers;
    private final CardMetricsClient cards;
    private final FraudMetricsClient fraud;

    @GetMapping("/api/admin/dashboard")
    public AdminDashboardResponse dashboard() {
        Map<String, Long> counts = cards.counts();
        return new AdminDashboardResponse(customers.count(), counts.get("accounts"),
                counts.get("cards"), counts.get("pendingLimits"), fraud.count());
    }
}
