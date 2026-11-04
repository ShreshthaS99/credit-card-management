package com.example.ccmps.controller;

import com.example.ccmps.dto.AdminLimitRequestResponse;
import com.example.ccmps.dto.LimitReviewRequest;
import com.example.ccmps.service.LimitReviewService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.ccmps.repository.CardAccountRepository;
import com.example.ccmps.repository.CreditCardRepository;
import com.example.ccmps.repository.LimitRequestRepository;

@RestController
@RequiredArgsConstructor
public class AdminLimitController {
    private final LimitReviewService reviews;
    private final CardAccountRepository accounts;
    private final CreditCardRepository cards;
    private final LimitRequestRepository requests;

    @GetMapping("/api/admin/limit-requests")
    public Page<AdminLimitRequestResponse> queue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviews.queue(page, size).map(AdminLimitRequestResponse::from);
    }

    @PostMapping("/api/admin/limit-requests/{requestId}/decision")
    public AdminLimitRequestResponse decide(@PathVariable UUID requestId,
            @Valid @RequestBody LimitReviewRequest request, Authentication authentication) {
        return AdminLimitRequestResponse.from(
                reviews.decide(requestId, request, authentication.getName()));
    }

    @GetMapping("/api/admin/metrics/cards")
    public Map<String, Long> counts() {
        return Map.of("accounts", accounts.count(), "cards", cards.count(),
                "pendingLimits", requests.countByStatus("PENDING"));
    }
}
