package com.example.ccmps.service;


import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CardAccountRepository;
import com.example.ccmps.security.AccessChecks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final CardAccountRepository accountRepository;
    private final AccessChecks access;

    public CardAccount getAccount(UUID accountId) {

        CardAccount account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Card account not found: " + accountId
                        )
                );
        access.customer(account.getCustomerId());
        return account;
    }

    public List<CardAccount> getCustomerAccounts(UUID customerId) {

        access.customer(customerId);
        return accountRepository.findByCustomerId(customerId);
    }
}