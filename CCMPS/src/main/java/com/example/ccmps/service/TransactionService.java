package com.example.ccmps.service;

import com.example.ccmps.entity.CardTransaction;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<CardTransaction> getTransactions(
            UUID accountId,
            int page,
            int size) {

        return transactionRepository
                .findByAccountIdOrderByTransactionTimeDesc(
                        accountId,
                        PageRequest.of(page, size)
                );
    }

    public CardTransaction getTransaction(UUID transactionId) {

        return transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found: "
                                        + transactionId
                        )
                );
    }
}