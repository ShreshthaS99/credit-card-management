package com.example.ccmps.contract;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DemoSeedRequest(@NotNull UUID customerId, UUID accountId, UUID transactionId) {
}
