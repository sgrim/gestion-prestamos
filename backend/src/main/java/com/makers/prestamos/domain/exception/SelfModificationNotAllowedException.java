package com.makers.prestamos.domain.exception;

/** Un administrador intenta desactivarse, borrarse o cambiarse el rol a sí mismo. */
public class SelfModificationNotAllowedException extends DomainException {

    public SelfModificationNotAllowedException(String message) {
        super(message);
    }
}
