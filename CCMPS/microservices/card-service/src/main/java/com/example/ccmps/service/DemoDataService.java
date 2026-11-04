package com.example.ccmps.service;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.entity.*;
import com.example.ccmps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DemoDataService {
    private final DemoSeedReceiptRepository receipts;
    private final CardAccountRepository accountRepository;
    private final CreditCardRepository cardRepository;

    @Transactional
    public DemoSeedResult provision(DemoSeedRequest request) {

        DemoSeedReceipt existing = receipts.findById(request.customerId()).orElse(null);
        if (existing != null) {
            return new DemoSeedResult(existing.getAccountId(), existing.getTransactionId());
        }
        // The unique receipt claims this customer before seeding, in the same local transaction.
        DemoSeedReceipt receipt = receipts.saveAndFlush(new DemoSeedReceipt(request.customerId()));

        CardAccount account = CardAccount.builder()
                .customerId(request.customerId())
                .creditLimit(new BigDecimal("200000.00"))
                .currentBalance(new BigDecimal("35450.75"))
                .minimumDue(new BigDecimal("2500.00"))
                .paymentDueDate(LocalDate.now().plusDays(15))
                .currency("INR")
                .status("ACTIVE")
                .build();

        account = accountRepository.save(account);

        CreditCard card = CreditCard.builder()
                .accountId(account.getId())
                .nickname("Platinum Card")
                .maskedPan("XXXX XXXX XXXX 4242")
                .tokenReference("TOKEN-DEMO-" + account.getId())
                .status("ISSUED")
                .onlineTransactionsEnabled(true)
                .contactlessEnabled(true)
                .internationalEnabled(false)
                .frozen(false)
                .build();

        cardRepository.save(card);


        DemoSeedResult result = new DemoSeedResult(account.getId(), null);
        receipt.setAccountId(result.accountId());
        receipt.setTransactionId(result.transactionId());
        receipts.save(receipt);
        return result;
    }
}
