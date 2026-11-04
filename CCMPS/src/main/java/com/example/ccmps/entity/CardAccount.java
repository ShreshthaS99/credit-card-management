package com.example.ccmps.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "card_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CardAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal minimumDue;

    private LocalDate paymentDueDate;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String status;
}
