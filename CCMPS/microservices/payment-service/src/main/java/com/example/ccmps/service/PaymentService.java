package com.example.ccmps.service;

import com.example.ccmps.client.AccountClient;
import com.example.ccmps.contract.AccountView;
import com.example.ccmps.dto.CreatePaymentRequest;
import com.example.ccmps.dto.PaymentResponse;
import com.example.ccmps.entity.Payment;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.PaymentRepository;
import com.example.ccmps.security.AccessChecks;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AccountClient accounts;
    private final AccessChecks access;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new BusinessException("A non-blank Idempotency-Key up to 255 characters is required.");
        }
        AccountView account = accounts.getAccount(request.accountId());
        Payment previous = paymentRepository.findByAccountIdAndIdempotencyKey(
                request.accountId(), idempotencyKey).orElse(null);
        if (previous != null) {
            if (previous.getAmount().compareTo(request.amount()) != 0
                    || !previous.getCurrency().equalsIgnoreCase(request.currency())) {
                throw new BusinessException("Idempotency-Key was already used with different details.");
            }
            return map(previous);
        }
        if (!account.currency().equalsIgnoreCase(request.currency())) {
            throw new BusinessException("Payment currency must match account currency.");
        }
        Payment payment = Payment.builder().accountId(request.accountId())
                .amount(request.amount()).currency(request.currency().toUpperCase(Locale.ROOT))
                .status("PENDING").idempotencyKey(idempotencyKey)
                .paymentReference("PAY-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return map(paymentRepository.save(payment));
    }

    public List<PaymentResponse> getPayments(UUID accountId) {
        access.account(accountId);
        return paymentRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(this::map).toList();
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new ResourceNotFoundException("Payment not found: " + paymentId));
        access.account(payment.getAccountId());
        return map(payment);
    }

    private PaymentResponse map(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getPaymentReference(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus(), payment.getCreatedAt());
    }
}
