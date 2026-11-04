package com.example.ccmps.repository;
import com.example.ccmps.entity.FraudAlert;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, UUID> {

    List<FraudAlert>
    findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    Page<FraudAlert>
    findByStatusInOrderByCreatedAtAsc(
            Collection<String> statuses,
            Pageable pageable
    );

    long countByStatusIn(Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select alert from FraudAlert alert where alert.id = :id")
    Optional<FraudAlert> findByIdForUpdate(@Param("id") UUID id);
}