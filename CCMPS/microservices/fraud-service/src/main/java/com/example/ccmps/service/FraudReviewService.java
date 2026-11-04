package com.example.ccmps.service;

import com.example.ccmps.dto.FraudReviewRequest;
import com.example.ccmps.entity.FraudAlert;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.FraudAlertRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FraudReviewService {
    private static final List<String> REVIEWABLE = List.of("OPEN", "REVIEW_REQUIRED");
    private final FraudAlertRepository alerts;

    public Page<FraudAlert> queue(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Page must be non-negative and size between 1 and 100.");
        }
        return alerts.findByStatusInOrderByCreatedAtAsc(REVIEWABLE, PageRequest.of(page, size));
    }

    public long count() {
        return alerts.countByStatusIn(REVIEWABLE);
    }

    @Transactional
    public FraudAlert decide(UUID alertId, FraudReviewRequest review, String actor) {
        FraudAlert alert = alerts.findByIdForUpdate(alertId).orElseThrow(
                () -> new ResourceNotFoundException("Fraud alert not found: " + alertId));
        if (!REVIEWABLE.contains(alert.getStatus())) {
            throw new BusinessException("Only open or review-required alerts can be reviewed.");
        }
        alert.setAnalystDisposition(review.disposition().name());
        alert.setAnalystNotes(review.notes().trim());
        alert.setReviewedBy(actor);
        alert.setReviewedAt(LocalDateTime.now());
        alert.setStatus("CLOSED");
        if (alert.getCaseReference() == null || alert.getCaseReference().isBlank()) {
            alert.setCaseReference("CASE-" + UUID.randomUUID());
        }
        return alerts.save(alert);
    }
}
