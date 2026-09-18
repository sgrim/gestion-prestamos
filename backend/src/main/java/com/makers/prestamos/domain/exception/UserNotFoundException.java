package com.makers.prestamos.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super("No existe el usuario con id " + id);
    }
}
