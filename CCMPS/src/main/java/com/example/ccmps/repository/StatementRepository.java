package com.example.ccmps.repository;

import com.example.ccmps.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface StatementRepository
        extends JpaRepository<Statement, UUID> {

    List<Statement>
    findByAccountIdOrderByCycleEndDesc(UUID accountId);
}