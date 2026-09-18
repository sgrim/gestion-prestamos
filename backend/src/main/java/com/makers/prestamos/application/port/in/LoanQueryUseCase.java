package com.makers.prestamos.application.port.in;

import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;

import java.util.List;

public interface LoanQueryUseCase {

    /** Devuelve el préstamo solo si pertenece al solicitante o este es administrador. */
    Loan getLoan(Long loanId, Long requesterId, boolean requesterIsAdmin);

    List<Loan> listByUser(Long userId);

    /** @param status filtro opcional; {@code null} devuelve todos. */
    List<Loan> listAll(LoanStatus status);
}
