package com.makers.prestamos.infrastructure.adapter.in.web.dto;

import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
        Long id,
        Long userId,
        String userEmail,
        BigDecimal amount,
        int termMonths,
        LoanStatus status,
        Instant requestedAt,
        Instant decidedAt
) {

    public static LoanResponse from(Loan loan, String userEmail) {
        return new LoanResponse(loan.id(), loan.userId(), userEmail, loan.amount(), loan.termMonths(),
                loan.status(), loan.requestedAt(), loan.decidedAt());
    }
}
