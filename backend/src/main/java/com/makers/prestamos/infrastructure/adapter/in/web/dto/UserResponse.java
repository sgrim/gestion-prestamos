package com.makers.prestamos.infrastructure.adapter.in.web.dto;

import com.makers.prestamos.domain.model.Role;
import com.makers.prestamos.domain.model.User;

import java.time.Instant;

/** Nunca expone el hash de la contraseña. */
public record UserResponse(Long id, String email, String fullName, Role role, boolean active, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.email(), user.fullName(), user.role(), user.active(), user.createdAt());
    }
}
