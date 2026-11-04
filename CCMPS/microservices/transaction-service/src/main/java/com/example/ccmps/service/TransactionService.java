package com.example.ccmps.service;

import com.example.ccmps.entity.CardTransaction;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.TransactionRepository;
import com.example.ccmps.security.AccessChecks;
import com.example.ccmps.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccessChecks access;

    public Page<CardTransaction> getTransactions(
            UUID accountId,
            int page,
            int size) {

        access.account(accountId);
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Page must be non-negative and size between 1 and 100.");
        }
        return transactionRepository
                .findByAccountIdOrderByTransactionTimeDesc(
                        accountId,
                        PageRequest.of(page, size)
                );
    }

    public CardTransaction getTransaction(UUID transactionId) {

        CardTransaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found: "
                                        + transactionId
                        )
                );
        access.account(transaction.getAccountId());
        return transaction;
    }
}