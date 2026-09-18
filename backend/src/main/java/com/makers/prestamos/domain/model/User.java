package com.makers.prestamos.domain.model;

import com.makers.prestamos.domain.exception.InvalidUserException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Usuario del sistema. {@code passwordHash} nunca contiene la contraseña en claro.
 */
public record User(
        Long id,
        String email,
        String passwordHash,
        String fullName,
        Role role,
        boolean active,
        Instant createdAt
) implements Serializable {

    public User {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new InvalidUserException("El email es obligatorio y debe ser válido");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidUserException("El nombre es obligatorio");
        }
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(role, "role");
        email = email.trim().toLowerCase();
        fullName = fullName.trim();
    }

    public static User register(String email, String passwordHash, String fullName, Role role, Instant now) {
        return new User(null, email, passwordHash, fullName, role, true, now);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /** Devuelve una copia con los datos de perfil actualizados (el email y la clave no cambian aquí). */
    public User withProfile(String newFullName, Role newRole, boolean newActive) {
        return new User(id, email, passwordHash, newFullName, newRole, newActive, createdAt);
    }

    public User deactivate() {
        return withProfile(fullName, role, false);
    }
}
