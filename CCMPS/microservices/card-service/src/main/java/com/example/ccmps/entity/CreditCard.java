package com.example.ccmps.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "credit_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String maskedPan;

    @Column(nullable = false)
    private String tokenReference;

    @Column(nullable = false)
    private String status;

    private Boolean onlineTransactionsEnabled;

    private Boolean contactlessEnabled;

    private Boolean internationalEnabled;

    private Boolean frozen;
}