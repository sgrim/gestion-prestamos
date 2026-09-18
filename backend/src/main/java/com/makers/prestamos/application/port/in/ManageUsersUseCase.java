package com.makers.prestamos.application.port.in;

import com.makers.prestamos.domain.model.Role;
import com.makers.prestamos.domain.model.User;

import java.util.List;

public interface ManageUsersUseCase {

    User create(CreateCommand command);

    User getById(Long id);

    List<User> listAll();

    /** TODO(candidato): implementar la actualización de datos básicos (nombre, rol, activo). */
    User update(UpdateCommand command);

    /** TODO(candidato): implementar la baja del usuario y decidir qué pasa con sus préstamos. */
    void delete(Long id);

    record CreateCommand(String email, String rawPassword, String fullName, Role role) {
    }

    record UpdateCommand(Long id, String fullName, Role role, boolean active) {
    }
}
