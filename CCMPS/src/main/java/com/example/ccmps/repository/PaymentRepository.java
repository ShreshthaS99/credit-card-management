package com.example.ccmps.repository;
import com.example.ccmps.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    Optional<Payment>
    findByAccountIdAndIdempotencyKey(
            UUID accountId,
            String idempotencyKey
    );

    List<Payment>
    findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}