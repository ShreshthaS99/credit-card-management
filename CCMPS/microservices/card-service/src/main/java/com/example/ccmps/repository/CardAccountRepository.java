package com.example.ccmps.repository;

import com.example.ccmps.entity.CardAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CardAccountRepository
        extends JpaRepository<CardAccount, UUID> {

    List<CardAccount> findByCustomerId(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from CardAccount account where account.id = :id")
    Optional<CardAccount> findByIdForUpdate(@Param("id") UUID id);
}