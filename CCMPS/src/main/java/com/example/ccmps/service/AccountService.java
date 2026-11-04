package com.example.ccmps.service;


import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CardAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final CardAccountRepository accountRepository;

    public CardAccount getAccount(UUID accountId) {

        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Card account not found: " + accountId
                        )
                );
    }

    public List<CardAccount> getCustomerAccounts(UUID customerId) {

        return accountRepository.findByCustomerId(customerId);
    }
}