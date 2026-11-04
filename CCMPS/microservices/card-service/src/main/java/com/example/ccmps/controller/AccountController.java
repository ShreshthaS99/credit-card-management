package com.example.ccmps.controller;

import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public CardAccount getAccount(
            @PathVariable UUID accountId) {

        return accountService.getAccount(accountId);
    }

    @GetMapping("/customer/{customerId}")
    public List<CardAccount> getCustomerAccounts(
            @PathVariable UUID customerId) {

        return accountService
                .getCustomerAccounts(customerId);
    }
}