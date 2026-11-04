package com.example.ccmps.controller;

import com.example.ccmps.dto.AdminFraudAlertResponse;
import com.example.ccmps.dto.FraudReviewRequest;
import com.example.ccmps.service.FraudReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminFraudController {
    private final FraudReviewService reviews;

    @GetMapping("/api/admin/fraud-alerts")
    public Page<AdminFraudAlertResponse> queue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviews.queue(page, size).map(AdminFraudAlertResponse::from);
    }

    @PostMapping("/api/admin/fraud-alerts/{alertId}/decision")
    public AdminFraudAlertResponse decide(@PathVariable UUID alertId,
            @Valid @RequestBody FraudReviewRequest request, Authentication authentication) {
        return AdminFraudAlertResponse.from(
                reviews.decide(alertId, request, authentication.getName()));
    }

    @GetMapping("/api/admin/metrics/fraud")
    public long count() {
        return reviews.count();
    }
}
