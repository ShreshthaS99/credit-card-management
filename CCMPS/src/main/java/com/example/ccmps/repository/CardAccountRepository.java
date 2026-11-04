package com.example.ccmps.repository;

import com.example.ccmps.entity.CardAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface CardAccountRepository
        extends JpaRepository<CardAccount, UUID> {

    List<CardAccount> findByCustomerId(UUID customerId);
}