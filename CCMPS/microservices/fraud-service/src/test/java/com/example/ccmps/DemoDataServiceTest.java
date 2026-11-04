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

@SpringBootTest(classes = FraudServiceApplication.class, properties = {
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
    @Autowired private FraudAlertRepository alerts;

    @Test
    void seedsOriginalSampleDataOnceAndPreservesChanges() {
        DemoSeedRequest request = new DemoSeedRequest(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        var first = demo.provision(request);
        var repeated = demo.provision(request);
        assertEquals(first, repeated);
        assertEquals(1, receipts.count());

        assertEquals(1, alerts.count());
        var alert = alerts.findAll().get(0);
        assertEquals(request.transactionId(), alert.getTransactionId());
        assertEquals("OPEN", alert.getStatus());
        alert.setStatus("CLOSED");
        alerts.saveAndFlush(alert);
        demo.provision(request);
        assertEquals("CLOSED", alerts.findById(alert.getId()).orElseThrow().getStatus());
    }

    @Test
    void differentCustomersGetIndependentData() {
        UUID accountId = UUID.randomUUID();
        var first = demo.provision(new DemoSeedRequest(UUID.randomUUID(), accountId,
                UUID.randomUUID()));
        var second = demo.provision(new DemoSeedRequest(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID()));
        assertEquals(2, receipts.count());
        assertNotEquals(first.accountId(), second.accountId());
    }
}
