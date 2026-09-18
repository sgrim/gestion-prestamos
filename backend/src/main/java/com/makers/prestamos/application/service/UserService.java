package com.makers.prestamos.application.service;

import com.makers.prestamos.application.port.in.ManageUsersUseCase;
import com.makers.prestamos.application.port.out.PasswordHasherPort;
import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.domain.exception.EmailAlreadyRegisteredException;
import com.makers.prestamos.domain.exception.SelfModificationNotAllowedException;
import com.makers.prestamos.domain.exception.UserNotFoundException;
import com.makers.prestamos.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional
public class UserService implements ManageUsersUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final Clock clock;

    public UserService(UserRepositoryPort users, PasswordHasherPort hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public User create(CreateCommand command) {
        String email = command.email() == null ? "" : command.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        User user = User.register(
                email, hasher.hash(command.rawPassword()), command.fullName(), command.role(), clock.instant());
        return users.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listAll() {
        return users.findAll();
    }

    @Override
    public User update(UpdateCommand command) {
        User user = getById(command.id());
        boolean isSelf = command.id().equals(command.requesterId());
        if (isSelf && (!command.active() || command.role() != user.role())) {
            throw new SelfModificationNotAllowedException("No puedes desactivarte ni cambiar tu propio rol");
        }
        return users.save(user.withProfile(command.fullName(), command.role(), command.active()));
    }

    @Override
    public void delete(Long id, Long requesterId) {
        User user = getById(id);
        if (id.equals(requesterId)) {
            throw new SelfModificationNotAllowedException("No puedes eliminar tu propio usuario");
        }
        if (user.active()) {
            users.save(user.deactivate());
        }
    }
}
