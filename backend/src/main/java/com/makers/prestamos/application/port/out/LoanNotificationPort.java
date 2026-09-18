package com.makers.prestamos.application.port.out;

import com.makers.prestamos.domain.model.Loan;

/** Avisa a sistemas externos de que un préstamo fue resuelto. Debe ser no bloqueante. */
public interface LoanNotificationPort {

    void loanDecided(Loan loan);
}
