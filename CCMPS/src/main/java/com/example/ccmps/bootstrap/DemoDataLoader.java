package com.example.ccmps.bootstrap;

import com.example.ccmps.entity.*;
        import com.example.ccmps.repository.*;
        import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final CardAccountRepository accountRepository;
    private final CreditCardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final StatementRepository statementRepository;
    private final StatementLineRepository statementLineRepository;
    private final RewardEntryRepository rewardRepository;
    private final FraudAlertRepository fraudRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() > 0) {
            return;
        }

        Customer customer = Customer.builder()
                .fullName("Aarav Sharma")
                .email("aarav.demo@example.com")
                .phone("9000000000")
                .status("ACTIVE")
                .build();

        customer = customerRepository.save(customer);

        CardAccount account = CardAccount.builder()
                .customerId(customer.getId())
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
                .tokenReference("TOKEN-DEMO-4242")
                .status("ISSUED")
                .onlineTransactionsEnabled(true)
                .contactlessEnabled(true)
                .internationalEnabled(false)
                .frozen(false)
                .build();

        cardRepository.save(card);

        CardTransaction transaction1 =
                CardTransaction.builder()
                        .accountId(account.getId())
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
                        .accountId(account.getId())
                        .merchant("Demo Grocery Store")
                        .amount(new BigDecimal("1250.50"))
                        .currency("INR")
                        .status("AUTHORIZED_PENDING")
                        .processorReference("TXN-DEMO-002")
                        .transactionTime(
                                LocalDateTime.now().minusHours(5)
                        )
                        .build();

        transactionRepository.save(transaction2);

        Statement statement =
                Statement.builder()
                        .accountId(account.getId())
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

        RewardEntry reward =
                RewardEntry.builder()
                        .accountId(account.getId())
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

        FraudAlert fraudAlert =
                FraudAlert.builder()
                        .accountId(account.getId())
                        .transactionId(
                                transaction2.getId()
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

        System.out.println(
                "==========================================="
        );

        System.out.println(
                "Demo data created successfully"
        );

        System.out.println(
                "Customer ID : " + customer.getId()
        );

        System.out.println(
                "Account ID  : " + account.getId()
        );

        System.out.println(
                "Card ID     : " + card.getId()
        );

        System.out.println(
                "==========================================="
        );
    }
}