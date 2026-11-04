package com.example.ccmps.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "limit_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LimitRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentLimit;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal requestedLimit;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status;

    private LocalDateTime requestedAt;

    @JsonIgnore
    @Column(length = 100)
    private String reviewedBy;

    @JsonIgnore
    private LocalDateTime reviewedAt;

    @JsonIgnore
    @Column(length = 500)
    private String reviewReason;
}