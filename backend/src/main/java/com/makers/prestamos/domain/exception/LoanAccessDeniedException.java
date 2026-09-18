package com.makers.prestamos.domain.exception;

public class LoanAccessDeniedException extends DomainException {

    public LoanAccessDeniedException(Long loanId) {
        super("No tienes acceso al préstamo " + loanId);
    }
}
