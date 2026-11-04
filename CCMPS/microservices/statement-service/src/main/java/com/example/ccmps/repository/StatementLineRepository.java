package com.example.ccmps.repository;
import com.example.ccmps.entity.StatementLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StatementLineRepository
        extends JpaRepository<StatementLine, UUID> {

    List<StatementLine> findByStatementId(UUID statementId);
}