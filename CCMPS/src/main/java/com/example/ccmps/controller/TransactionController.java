package com.example.ccmps.controller;

import com.example.ccmps.entity.CardTransaction;
import com.example.ccmps.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

        import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public Page<CardTransaction> getTransactions(
            @RequestParam UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return transactionService
                .getTransactions(accountId, page, size);
    }

    @GetMapping("/{transactionId}")
    public CardTransaction getTransaction(
            @PathVariable UUID transactionId) {

        return transactionService
                .getTransaction(transactionId);
    }
}