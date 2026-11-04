package com.example.ccmps.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "statements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private LocalDate cycleStart;

    @Column(nullable = false)
    private LocalDate cycleEnd;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal openingBalance;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal closingBalance;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal minimumDue;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer version;
}