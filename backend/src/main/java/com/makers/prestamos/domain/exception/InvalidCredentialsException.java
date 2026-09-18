package com.makers.prestamos.domain.exception;

/** Mensaje deliberadamente genérico para no revelar si el email existe. */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
