package com.example.ccmps.repository;
import com.example.ccmps.entity.LimitRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LimitRequestRepository
        extends JpaRepository<LimitRequest, UUID> {

    List<LimitRequest>
    findByAccountIdOrderByRequestedAtDesc(UUID accountId);

    Page<LimitRequest>
    findByStatusOrderByRequestedAtAsc(
            String status,
            Pageable pageable
    );

    long countByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from LimitRequest request where request.id = :id")
    Optional<LimitRequest> findByIdForUpdate(@Param("id") UUID id);
}