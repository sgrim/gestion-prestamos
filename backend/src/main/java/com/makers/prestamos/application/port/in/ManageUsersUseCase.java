package com.makers.prestamos.application.port.in;

import com.makers.prestamos.domain.model.Role;
import com.makers.prestamos.domain.model.User;

import java.util.List;

public interface ManageUsersUseCase {

    User create(CreateCommand command);

    User getById(Long id);

    List<User> listAll();

    /** Actualiza nombre, rol y estado. Un administrador no puede desactivarse ni cambiarse el rol a sí mismo. */
    User update(UpdateCommand command);

    /**
     * Baja lógica: el usuario queda inactivo (no puede iniciar sesión) pero se conserva su historial de
     * préstamos. Es idempotente y un administrador no puede darse de baja a sí mismo.
     */
    void delete(Long id, Long requesterId);

    record CreateCommand(String email, String rawPassword, String fullName, Role role) {
    }

    record UpdateCommand(Long id, String fullName, Role role, boolean active, Long requesterId) {
    }
}
