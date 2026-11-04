package com.example.ccmps.service;

import com.example.ccmps.dto.LimitRequestDto;
import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.entity.LimitRequest;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CardAccountRepository;
import com.example.ccmps.repository.LimitRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LimitService {

    private final LimitRequestRepository limitRequestRepository;
    private final AccountService accounts;

    public LimitRequest createRequest(
            LimitRequestDto request) {

        CardAccount account = accounts.getAccount(request.accountId());

        if (request.requestedLimit()
                .compareTo(account.getCreditLimit()) <= 0) {

            throw new BusinessException(
                    "Requested limit must be greater than current limit."
            );
        }

        LimitRequest limitRequest =
                LimitRequest.builder()
                        .accountId(account.getId())
                        .currentLimit(account.getCreditLimit())
                        .requestedLimit(request.requestedLimit())
                        .reason(request.reason())
                        .status("PENDING")
                        .requestedAt(LocalDateTime.now())
                        .build();

        return limitRequestRepository.save(limitRequest);
    }

    public List<LimitRequest> getRequests(UUID accountId) {

        accounts.getAccount(accountId);
        return limitRequestRepository
                .findByAccountIdOrderByRequestedAtDesc(accountId);
    }
}