package com.makers.prestamos.domain.exception;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("Ya existe un usuario con el email " + email);
    }
}
