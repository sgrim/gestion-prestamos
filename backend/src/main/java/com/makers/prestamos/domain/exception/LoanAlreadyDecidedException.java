package com.makers.prestamos.domain.exception;

import com.makers.prestamos.domain.model.LoanStatus;

public class LoanAlreadyDecidedException extends DomainException {

    public LoanAlreadyDecidedException(Long id, LoanStatus currentStatus) {
        super("El préstamo %d ya fue resuelto (estado actual: %s)".formatted(id, currentStatus));
    }
}
