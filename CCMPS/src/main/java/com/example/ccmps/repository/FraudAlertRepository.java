package com.example.ccmps.repository;
import com.example.ccmps.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, UUID> {

    List<FraudAlert>
    findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}