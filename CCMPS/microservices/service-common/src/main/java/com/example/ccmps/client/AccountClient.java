package com.example.ccmps.client;

import com.example.ccmps.contract.AccountView;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "card-service", contextId = "accountClient")
public interface AccountClient {
    @GetMapping("/api/accounts/{accountId}")
    AccountView getAccount(@PathVariable("accountId") UUID accountId);
}
