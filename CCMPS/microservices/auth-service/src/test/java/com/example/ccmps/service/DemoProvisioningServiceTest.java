package com.example.ccmps.service;

import com.example.ccmps.client.*;
import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.security.TokenService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoProvisioningServiceTest {
    @Mock private CardDemoClient cards;
    @Mock private TransactionDemoClient transactions;
    @Mock private StatementDemoClient statements;
    @Mock private RewardsDemoClient rewards;
    @Mock private FraudDemoClient fraud;
    @Mock private TokenService tokens;
    @InjectMocks private DemoProvisioningService service;

    @Test
    void provisionsLinkedRecordsInOwnerOrder() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        when(tokens.provisioningToken()).thenReturn("Bearer test-system");
        when(cards.provision(new DemoSeedRequest(customerId, null, null), "Bearer test-system"))
                .thenReturn(new DemoSeedResult(accountId, null));
        when(transactions.provision(new DemoSeedRequest(customerId, accountId, null), "Bearer test-system"))
                .thenReturn(new DemoSeedResult(accountId, transactionId));

        service.provision(customerId);

        DemoSeedRequest linked = new DemoSeedRequest(customerId, accountId, transactionId);
        var ordered = inOrder(cards, transactions, statements, rewards, fraud);
        ordered.verify(cards).provision(new DemoSeedRequest(customerId, null, null), "Bearer test-system");
        ordered.verify(transactions).provision(new DemoSeedRequest(customerId, accountId, null), "Bearer test-system");
        ordered.verify(statements).provision(linked, "Bearer test-system");
        ordered.verify(rewards).provision(linked, "Bearer test-system");
        ordered.verify(fraud).provision(linked, "Bearer test-system");
    }

    @Test
    void dependencyFailureIsNotHidden() {
        when(tokens.provisioningToken()).thenReturn("Bearer test-system");
        when(cards.provision(any(), anyString())).thenThrow(new IllegalStateException("Unavailable"));

        assertThrows(IllegalStateException.class, () -> service.provision(UUID.randomUUID()));
        verifyNoInteractions(transactions, statements, rewards, fraud);
    }

    @Test
    void canDisableDemoProvisioning() {
        ReflectionTestUtils.setField(service, "enabled", false);
        service.provision(UUID.randomUUID());
        verifyNoInteractions(cards, transactions, statements, rewards, fraud, tokens);
    }
}
