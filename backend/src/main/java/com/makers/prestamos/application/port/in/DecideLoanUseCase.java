package com.makers.prestamos.application.port.in;

import com.makers.prestamos.domain.model.Loan;

/** Caso de uso exclusivo de administradores. */
public interface DecideLoanUseCase {

    Loan approve(Long loanId, Long adminId);

    Loan reject(Long loanId, Long adminId);
}
