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

@SpringBootTest(classes = RewardsServiceApplication.class, properties = {
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
    @Autowired private RewardEntryRepository rewards;

    @Test
    void seedsOriginalSampleDataOnceAndPreservesChanges() {
        DemoSeedRequest request = new DemoSeedRequest(UUID.randomUUID(), UUID.randomUUID(),
                null);
        var first = demo.provision(request);
        var repeated = demo.provision(request);
        assertEquals(first, repeated);
        assertEquals(1, receipts.count());

        assertEquals(1, rewards.count());
        var reward = rewards.findAll().get(0);
        assertEquals(500, reward.getPoints());
        reward.setPoints(400);
        rewards.saveAndFlush(reward);
        demo.provision(request);
        assertEquals(400, rewards.findById(reward.getId()).orElseThrow().getPoints());
    }

    @Test
    void differentCustomersGetIndependentData() {
        UUID accountId = UUID.randomUUID();
        var first = demo.provision(new DemoSeedRequest(UUID.randomUUID(), accountId,
                null));
        var second = demo.provision(new DemoSeedRequest(UUID.randomUUID(), UUID.randomUUID(),
                null));
        assertEquals(2, receipts.count());
        assertNotEquals(first.accountId(), second.accountId());
    }
}
