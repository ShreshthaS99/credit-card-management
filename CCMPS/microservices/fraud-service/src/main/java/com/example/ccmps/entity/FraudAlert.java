package com.example.ccmps.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    @Column(length = 30)
    private String analystDisposition;

    @JsonIgnore
    @Column(length = 1000)
    private String analystNotes;

    @JsonIgnore
    @Column(length = 100)
    private String reviewedBy;

    @JsonIgnore
    private LocalDateTime reviewedAt;
}