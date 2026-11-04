package com.example.ccmps.repository;

import com.example.ccmps.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditCardRepository
        extends JpaRepository<CreditCard, UUID> {

    List<CreditCard> findByAccountId(UUID accountId);
}