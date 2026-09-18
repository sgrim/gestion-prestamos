package com.makers.prestamos.domain.exception;

public class LoanNotFoundException extends DomainException {

    public LoanNotFoundException(Long id) {
        super("No existe el préstamo con id " + id);
    }
}
