package com.makers.prestamos.application.port.out;

import com.makers.prestamos.domain.model.User;

import java.time.Instant;

public interface TokenIssuerPort {

    IssuedToken issue(User user);

    record IssuedToken(String value, Instant expiresAt) {
    }
}
