package com.example.ccmps;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.repository.*;
import com.example.ccmps.service.DemoDataService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CardServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:demo-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "app.jwt.secret=test-only-signing-key-with-at-least-thirty-two-bytes"
})
@Transactional
class DemoDataServiceTest {
    @Autowired private DemoDataService demo;
    @Autowired private DemoSeedReceiptRepository receipts;
    @Autowired private CardAccountRepository accounts;
    @Autowired private CreditCardRepository cards;

    @Test
    void seedsOriginalSampleDataOnceAndPreservesChanges() {
        DemoSeedRequest request = new DemoSeedRequest(UUID.randomUUID(), null,
                null);
        var first = demo.provision(request);
        var repeated = demo.provision(request);
        assertEquals(first, repeated);
        assertEquals(1, receipts.count());

        assertEquals(1, accounts.count());
        assertEquals(1, cards.count());
        var account = accounts.findById(first.accountId()).orElseThrow();
        assertEquals(new java.math.BigDecimal("200000.00"), account.getCreditLimit());
        assertEquals(new java.math.BigDecimal("35450.75"), account.getCurrentBalance());
        assertEquals(new java.math.BigDecimal("2500.00"), account.getMinimumDue());
        assertEquals(java.time.LocalDate.now().plusDays(15), account.getPaymentDueDate());
        var card = cards.findByAccountId(first.accountId()).get(0);
        assertEquals("ISSUED", card.getStatus());
        card.setStatus("ACTIVE");
        cards.saveAndFlush(card);
        demo.provision(request);
        assertEquals("ACTIVE", cards.findById(card.getId()).orElseThrow().getStatus());
    }

    @Test
    void differentCustomersGetIndependentData() {
        UUID accountId = null;
        var first = demo.provision(new DemoSeedRequest(UUID.randomUUID(), accountId,
                null));
        var second = demo.provision(new DemoSeedRequest(UUID.randomUUID(), null,
                null));
        assertEquals(2, receipts.count());
        assertNotEquals(first.accountId(), second.accountId());
    }
}
