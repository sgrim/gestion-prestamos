package com.makers.prestamos.application.port.in;

import com.makers.prestamos.domain.model.Loan;

import java.math.BigDecimal;

public interface RequestLoanUseCase {

    Loan requestLoan(Command command);

    record Command(Long userId, BigDecimal amount, int termMonths) {
    }
}
