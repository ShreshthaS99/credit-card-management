package com.example.ccmps.controller;

import com.example.ccmps.entity.Statement;
import com.example.ccmps.entity.StatementLine;
import com.example.ccmps.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping
    public List<Statement> getStatements(
            @RequestParam UUID accountId) {

        return statementService
                .getStatements(accountId);
    }

    @GetMapping("/{statementId}")
    public Statement getStatement(
            @PathVariable UUID statementId) {

        return statementService
                .getStatement(statementId);
    }

    @GetMapping("/{statementId}/lines")
    public List<StatementLine> getLines(
            @PathVariable UUID statementId) {

        return statementService
                .getStatementLines(statementId);
    }
}