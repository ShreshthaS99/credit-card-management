package com.example.ccmps.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "demo_seed_receipts")
@Getter
@Setter
@NoArgsConstructor
public class DemoSeedReceipt {
    @Id
    private UUID customerId;
    private UUID accountId;
    private UUID transactionId;
    @Version
    private Long version;

    public DemoSeedReceipt(UUID customerId) {
        this.customerId = customerId;
    }
}
