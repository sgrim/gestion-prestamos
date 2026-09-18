package com.makers.prestamos.domain.exception;

/** Raíz de las excepciones de negocio; la capa web las traduce a respuestas HTTP. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
