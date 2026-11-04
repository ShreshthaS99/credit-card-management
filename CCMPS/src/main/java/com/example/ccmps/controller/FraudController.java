package com.example.ccmps.controller;

import com.example.ccmps.dto.FraudResponseRequest;
import com.example.ccmps.entity.FraudAlert;
import com.example.ccmps.service.FraudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudService fraudService;

    @GetMapping("/alerts")
    public List<FraudAlert> getAlerts(
            @RequestParam UUID accountId) {

        return fraudService.getAlerts(accountId);
    }

    @GetMapping("/alerts/{alertId}")
    public FraudAlert getAlert(
            @PathVariable UUID alertId) {

        return fraudService.getAlert(alertId);
    }

    @PostMapping("/alerts/{alertId}/response")
    public FraudAlert respond(
            @PathVariable UUID alertId,
            @Valid
            @RequestBody
            FraudResponseRequest request) {

        return fraudService.respond(
                alertId,
                request
        );
    }
}