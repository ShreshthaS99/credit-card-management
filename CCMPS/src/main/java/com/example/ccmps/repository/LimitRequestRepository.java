package com.example.ccmps.repository;
import com.example.ccmps.entity.LimitRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LimitRequestRepository
        extends JpaRepository<LimitRequest, UUID> {

    List<LimitRequest>
    findByAccountIdOrderByRequestedAtDesc(UUID accountId);
}