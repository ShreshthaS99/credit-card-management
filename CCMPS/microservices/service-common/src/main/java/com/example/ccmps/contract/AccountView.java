package com.example.ccmps.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountView(UUID id, UUID customerId, BigDecimal creditLimit,
                          BigDecimal currentBalance, String currency, String status) {
}
