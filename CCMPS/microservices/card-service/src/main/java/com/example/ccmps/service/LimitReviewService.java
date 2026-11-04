package com.example.ccmps.service;

import com.example.ccmps.dto.LimitDecision;
import com.example.ccmps.dto.LimitReviewRequest;
import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.entity.LimitRequest;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CardAccountRepository;
import com.example.ccmps.repository.LimitRequestRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LimitReviewService {
    private final LimitRequestRepository requests;
    private final CardAccountRepository accounts;

    public Page<LimitRequest> queue(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Page must be non-negative and size between 1 and 100.");
        }
        return requests.findByStatusOrderByRequestedAtAsc("PENDING", PageRequest.of(page, size));
    }

    @Transactional
    public LimitRequest decide(UUID requestId, LimitReviewRequest review, String actor) {
        LimitRequest request = requests.findByIdForUpdate(requestId).orElseThrow(
                () -> new ResourceNotFoundException("Limit request not found: " + requestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("Only pending limit requests can be reviewed.");
        }
        if (review.decision() == LimitDecision.APPROVE) {
            CardAccount account = accounts.findByIdForUpdate(request.getAccountId()).orElseThrow(
                    () -> new ResourceNotFoundException("Card account not found."));
            if (account.getCreditLimit().compareTo(request.getCurrentLimit()) != 0) {
                throw new BusinessException("Account limit changed after this request was submitted.");
            }
            if (request.getRequestedLimit().compareTo(account.getCreditLimit()) <= 0) {
                throw new BusinessException("Requested limit must exceed the current limit.");
            }
            account.setCreditLimit(request.getRequestedLimit());
            accounts.save(account);
            request.setStatus("APPROVED");
        } else {
            request.setStatus("DECLINED");
        }
        request.setReviewedBy(actor);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewReason(review.reason().trim());
        return requests.save(request);
    }
}
