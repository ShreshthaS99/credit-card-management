package com.example.ccmps.security;

import com.example.ccmps.client.AccountClient;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AccessChecks {
    private final AccountClient accounts;

    public AccessChecks(AccountClient accounts) {
        this.accounts = accounts;
    }

    public void customer(UUID customerId) {
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken authentication) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                return;
            }
            if (customerId != null && customerId.toString()
                    .equals(authentication.getToken().getClaimAsString("customerId"))) {
                return;
            }
        }
        throw new AccessDeniedException("You do not have access to this customer.");
    }

    public void account(UUID accountId) {
        // The account owner validates this same bearer token before returning data.
        accounts.getAccount(accountId);
    }
}
