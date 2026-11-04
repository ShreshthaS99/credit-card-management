package com.example.ccmps.service;

import com.example.ccmps.dto.RewardRedemptionRequest;
import com.example.ccmps.entity.RewardEntry;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.repository.RewardEntryRepository;
import com.example.ccmps.security.AccessChecks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardsService {

    private final RewardEntryRepository rewardRepository;
    private final AccessChecks access;

    public List<RewardEntry> getLedger(UUID accountId) {

        access.account(accountId);
        return rewardRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public int getBalance(UUID accountId) {

        access.account(accountId);
        return rewardRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .mapToInt(RewardEntry::getPoints)
                .sum();
    }

    @Transactional
    public RewardEntry redeem(
            RewardRedemptionRequest request) {

        int availablePoints = getBalance(request.accountId());

        if (request.points() > availablePoints) {

            throw new BusinessException(
                    "Insufficient reward points."
            );
        }

        RewardEntry redemption =
                RewardEntry.builder()
                        .accountId(request.accountId())
                        .type("REDEEMED")
                        .points(-request.points())
                        .description("Reward points redemption")
                        .sourceReference(
                                "RDM-"
                                        + UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase()
                        )
                        .createdAt(LocalDateTime.now())
                        .build();

        return rewardRepository.save(redemption);
    }
}