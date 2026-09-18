package com.makers.prestamos.infrastructure.adapter.in.web;

import org.springframework.security.oauth2.jwt.Jwt;

/** Extrae los datos del usuario autenticado a partir del JWT ya validado por Spring Security. */
record CurrentUser(Long id, boolean admin) {

    static CurrentUser from(Jwt jwt) {
        return new CurrentUser(Long.valueOf(jwt.getSubject()), "ADMIN".equals(jwt.getClaimAsString("role")));
    }
}
