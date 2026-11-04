package com.example.ccmps.controller;

import com.example.ccmps.dto.RewardRedemptionRequest;
import com.example.ccmps.entity.RewardEntry;
import com.example.ccmps.service.RewardsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardService;

    @GetMapping("/ledger")
    public List<RewardEntry> getLedger(
            @RequestParam UUID accountId) {

        return rewardService.getLedger(accountId);
    }

    @GetMapping("/balance")
    public Map<String, Integer> getBalance(
            @RequestParam UUID accountId) {

        return Map.of(
                "points",
                rewardService.getBalance(accountId)
        );
    }

    @PostMapping("/redemptions")
    public RewardEntry redeem(
            @Valid
            @RequestBody
            RewardRedemptionRequest request) {

        return rewardService.redeem(request);
    }
}