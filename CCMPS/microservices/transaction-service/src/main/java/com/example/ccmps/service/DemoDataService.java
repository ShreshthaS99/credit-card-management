package com.example.ccmps.service;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.entity.*;
import com.example.ccmps.repository.*;
import com.example.ccmps.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DemoDataService {
    private final DemoSeedReceiptRepository receipts;
    private final TransactionRepository transactionRepository;

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

        CardTransaction transaction1 =
                CardTransaction.builder()
                        .accountId(request.accountId())
                        .merchant("Demo Electronics Store")
                        .amount(new BigDecimal("4599.00"))
                        .currency("INR")
                        .status("POSTED")
                        .processorReference("TXN-DEMO-001")
                        .transactionTime(
                                LocalDateTime.now().minusDays(2)
                        )
                        .build();

        transactionRepository.save(transaction1);

        CardTransaction transaction2 =
                CardTransaction.builder()
                        .accountId(request.accountId())
                        .merchant("Demo Grocery Store")
                        .amount(new BigDecimal("1250.50"))
                        .currency("INR")
                        .status("AUTHORIZED_PENDING")
                        .processorReference("TXN-DEMO-002")
                        .transactionTime(
                                LocalDateTime.now().minusHours(5)
                        )
                        .build();

        transaction2 = transactionRepository.save(transaction2);


        DemoSeedResult result = new DemoSeedResult(request.accountId(), transaction2.getId());
        receipt.setAccountId(result.accountId());
        receipt.setTransactionId(result.transactionId());
        receipts.save(receipt);
        return result;
    }
}
