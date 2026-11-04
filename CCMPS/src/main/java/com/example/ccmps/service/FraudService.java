package com.example.ccmps.service;

import com.example.ccmps.dto.FraudResponseRequest;
import com.example.ccmps.entity.FraudAlert;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudService {

    private final FraudAlertRepository fraudRepository;

    private static final Set<String> VALID_RESPONSES =
            Set.of(
                    "CONFIRMED",
                    "NOT_RECOGNIZED"
            );

    public List<FraudAlert> getAlerts(UUID accountId) {

        return fraudRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public FraudAlert getAlert(UUID alertId) {

        return fraudRepository
                .findById(alertId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Fraud alert not found: " + alertId
                        )
                );
    }

    @Transactional
    public FraudAlert respond(
            UUID alertId,
            FraudResponseRequest request) {

        FraudAlert alert = getAlert(alertId);

        String response =
                request.response().trim().toUpperCase();

        if (!VALID_RESPONSES.contains(response)) {

            throw new BusinessException(
                    "Response must be CONFIRMED or NOT_RECOGNIZED."
            );
        }

        alert.setCustomerResponse(response);
        alert.setRespondedAt(LocalDateTime.now());

        if ("NOT_RECOGNIZED".equals(response)) {

            alert.setStatus("REVIEW_REQUIRED");

            alert.setCaseReference(
                    "CASE-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase()
            );

        } else {

            alert.setStatus("CLOSED");
        }

        return fraudRepository.save(alert);
    }
}