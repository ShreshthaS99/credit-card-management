package com.example.ccmps.service;

import com.example.ccmps.client.CardDemoClient;
import com.example.ccmps.client.FraudDemoClient;
import com.example.ccmps.client.RewardsDemoClient;
import com.example.ccmps.client.StatementDemoClient;
import com.example.ccmps.client.TransactionDemoClient;
import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.security.TokenService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoProvisioningService {
    private final CardDemoClient cards;
    private final TransactionDemoClient transactions;
    private final StatementDemoClient statements;
    private final RewardsDemoClient rewards;
    private final FraudDemoClient fraud;
    private final TokenService tokens;

    @Value("${app.demo.enabled:true}")
    private boolean enabled = true;

    public void provision(UUID customerId) {
        if (!enabled) {
            return;
        }
        String authorization = tokens.provisioningToken();
        DemoSeedResult account = cards.provision(
                new DemoSeedRequest(customerId, null, null), authorization);
        DemoSeedResult transaction = transactions.provision(
                new DemoSeedRequest(customerId, account.accountId(), null), authorization);
        DemoSeedRequest linked = new DemoSeedRequest(
                customerId, account.accountId(), transaction.transactionId());
        statements.provision(linked, authorization);
        rewards.provision(linked, authorization);
        fraud.provision(linked, authorization);
    }
}
