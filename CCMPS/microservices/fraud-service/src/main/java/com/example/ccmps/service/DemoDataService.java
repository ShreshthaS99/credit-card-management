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
    private final FraudAlertRepository fraudRepository;

    @Transactional
    public DemoSeedResult provision(DemoSeedRequest request) {

        if (request.accountId() == null || request.transactionId() == null) {
            throw new BusinessException("Demo provisioning requires the linked account and transaction.");
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

        FraudAlert fraudAlert =
                FraudAlert.builder()
                        .accountId(request.accountId())
                        .transactionId(
                                request.transactionId()
                        )
                        .riskReasonCode(
                                "UNUSUAL_TRANSACTION"
                        )
                        .status("OPEN")
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        fraudRepository.save(fraudAlert);


        DemoSeedResult result = new DemoSeedResult(request.accountId(), request.transactionId());
        receipt.setAccountId(result.accountId());
        receipt.setTransactionId(result.transactionId());
        receipts.save(receipt);
        return result;
    }
}
