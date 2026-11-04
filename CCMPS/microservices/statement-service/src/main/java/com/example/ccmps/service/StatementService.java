package com.example.ccmps.service;

import com.example.ccmps.entity.Statement;
import com.example.ccmps.entity.StatementLine;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.StatementLineRepository;
import com.example.ccmps.repository.StatementRepository;
import com.example.ccmps.security.AccessChecks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final StatementRepository statementRepository;
    private final StatementLineRepository statementLineRepository;
    private final AccessChecks access;

    public List<Statement> getStatements(UUID accountId) {

        access.account(accountId);
        return statementRepository
                .findByAccountIdOrderByCycleEndDesc(accountId);
    }

    public Statement getStatement(UUID statementId) {

        Statement statement = statementRepository
                .findById(statementId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Statement not found: "
                                        + statementId
                        )
                );
        access.account(statement.getAccountId());
        return statement;
    }

    public List<StatementLine> getStatementLines(
            UUID statementId) {

        getStatement(statementId);

        return statementLineRepository
                .findByStatementId(statementId);
    }
}