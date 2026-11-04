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
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DemoDataService {
    private final DemoSeedReceiptRepository receipts;
    private final StatementRepository statementRepository;
    private final StatementLineRepository statementLineRepository;

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

        Statement statement =
                Statement.builder()
                        .accountId(request.accountId())
                        .cycleStart(
                                LocalDate.now()
                                        .minusMonths(1)
                        )
                        .cycleEnd(
                                LocalDate.now()
                                        .minusDays(1)
                        )
                        .openingBalance(
                                new BigDecimal("12000.00")
                        )
                        .closingBalance(
                                new BigDecimal("35450.75")
                        )
                        .minimumDue(
                                new BigDecimal("2500.00")
                        )
                        .dueDate(
                                LocalDate.now().plusDays(15)
                        )
                        .status("PUBLISHED")
                        .version(1)
                        .build();

        statement =
                statementRepository.save(statement);

        StatementLine statementLine =
                StatementLine.builder()
                        .statementId(statement.getId())
                        .transactionDate(
                                LocalDate.now().minusDays(2)
                        )
                        .description(
                                "Demo Electronics Store"
                        )
                        .amount(
                                new BigDecimal("4599.00")
                        )
                        .type("PURCHASE")
                        .build();

        statementLineRepository.save(statementLine);


        DemoSeedResult result = new DemoSeedResult(request.accountId(), request.transactionId());
        receipt.setAccountId(result.accountId());
        receipt.setTransactionId(result.transactionId());
        receipts.save(receipt);
        return result;
    }
}
