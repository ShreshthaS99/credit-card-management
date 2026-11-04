package com.example.ccmps.service;

import com.example.ccmps.dto.CreatePaymentRequest;
import com.example.ccmps.dto.PaymentResponse;
import com.example.ccmps.entity.CardAccount;
import com.example.ccmps.entity.Payment;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CardAccountRepository;
import com.example.ccmps.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CardAccountRepository accountRepository;

    @Transactional
    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String idempotencyKey) {

        Payment previousPayment =
                paymentRepository
                        .findByAccountIdAndIdempotencyKey(
                                request.accountId(),
                                idempotencyKey
                        )
                        .orElse(null);

        if (previousPayment != null) {
            return map(previousPayment);
        }

        CardAccount account =
                accountRepository
                        .findById(request.accountId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Card account not found."
                                )
                        );

        if (!account.getCurrency()
                .equalsIgnoreCase(request.currency())) {

            throw new BusinessException(
                    "Payment currency must match account currency."
            );
        }

        Payment payment = Payment.builder()
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .status("PENDING")
                .idempotencyKey(idempotencyKey)
                .paymentReference(generateReference())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        return map(savedPayment);
    }

    public List<PaymentResponse> getPayments(UUID accountId) {

        return paymentRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::map)
                .toList();
    }

    public PaymentResponse getPayment(UUID paymentId) {

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found: " + paymentId
                        )
                );

        return map(payment);
    }

    private String generateReference() {

        return "PAY-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private PaymentResponse map(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}