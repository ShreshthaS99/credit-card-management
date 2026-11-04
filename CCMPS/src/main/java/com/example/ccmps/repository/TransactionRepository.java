package com.example.ccmps.repository;
import com.example.ccmps.entity.CardTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<CardTransaction, UUID> {

    Page<CardTransaction>
    findByAccountIdOrderByTransactionTimeDesc(
            UUID accountId,
            Pageable pageable
    );
}