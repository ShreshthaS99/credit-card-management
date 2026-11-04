package com.example.ccmps.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    private UUID transactionId;

    @Column(nullable = false)
    private String riskReasonCode;

    @Column(nullable = false)
    private String status;

    private String customerResponse;

    private String caseReference;

    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;
}