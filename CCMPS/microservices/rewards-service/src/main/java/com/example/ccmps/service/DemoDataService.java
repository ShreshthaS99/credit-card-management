package com.example.ccmps.service;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.entity.*;
import com.example.ccmps.repository.*;
import com.example.ccmps.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DemoDataService {
    private final DemoSeedReceiptRepository receipts;
    private final RewardEntryRepository rewardRepository;

    @Transactional
    public DemoSeedResult provision(DemoSeedRequest request) {

        if (request.accountId() == null) {
            throw new BusinessException("Demo provisioning requires the linked account.");
        }

        DemoSeedReceipt existing = receipts.findById(request.customerId()).orElse(null);
        if (existing != null) {
            if (!java.util.Objects.equals(existing.getAccountId(), request.accountId())) {
                throw new BusinessException("Demo data is already linked to another account.");
            }
            return new DemoSeedResult(existing.getAccountId(), existing.getTransactionId());
        }
        // The unique receipt claims this customer before seeding, in the same local transaction.
        DemoSeedReceipt receipt = receipts.saveAndFlush(new DemoSeedReceipt(request.customerId()));

        RewardEntry reward =
                RewardEntry.builder()
                        .accountId(request.accountId())
                        .type("EARNED")
                        .points(500)
                        .sourceReference("REWARD-DEMO-001")
                        .description(
                                "Demo purchase reward"
                        )
                        .createdAt(
                                LocalDateTime.now()
                                        .minusDays(2)
                        )
                        .build();

        rewardRepository.save(reward);


        DemoSeedResult result = new DemoSeedResult(request.accountId(), request.transactionId());
        receipt.setAccountId(result.accountId());
        receipt.setTransactionId(result.transactionId());
        receipts.save(receipt);
        return result;
    }
}
