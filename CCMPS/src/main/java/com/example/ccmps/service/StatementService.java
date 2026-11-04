package com.example.ccmps.service;

import com.example.ccmps.entity.Statement;
import com.example.ccmps.entity.StatementLine;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.StatementLineRepository;
import com.example.ccmps.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final StatementRepository statementRepository;
    private final StatementLineRepository statementLineRepository;

    public List<Statement> getStatements(UUID accountId) {

        return statementRepository
                .findByAccountIdOrderByCycleEndDesc(accountId);
    }

    public Statement getStatement(UUID statementId) {

        return statementRepository
                .findById(statementId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Statement not found: "
                                        + statementId
                        )
                );
    }

    public List<StatementLine> getStatementLines(
            UUID statementId) {

        getStatement(statementId);

        return statementLineRepository
                .findByStatementId(statementId);
    }
}