package com.makers.prestamos.application;

import com.makers.prestamos.application.port.in.ManageUsersUseCase.CreateCommand;
import com.makers.prestamos.application.port.in.ManageUsersUseCase.UpdateCommand;
import com.makers.prestamos.application.port.out.PasswordHasherPort;
import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.application.service.UserService;
import com.makers.prestamos.domain.exception.EmailAlreadyRegisteredException;
import com.makers.prestamos.domain.exception.SelfModificationNotAllowedException;
import com.makers.prestamos.domain.exception.UserNotFoundException;
import com.makers.prestamos.domain.model.Role;
import com.makers.prestamos.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Long ADMIN_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock UserRepositoryPort users;
    @Mock PasswordHasherPort hasher;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(users, hasher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createHashesThePasswordAndNormalizesTheEmail() {
        when(users.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(hasher.hash("secreta")).thenReturn("hash-de-secreta");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = service.create(new CreateCommand(" Nuevo@Test.com ", "secreta", "Nuevo", Role.USER));

        assertThat(created.email()).isEqualTo("nuevo@test.com");
        assertThat(created.passwordHash()).isEqualTo("hash-de-secreta");
        assertThat(created.active()).isTrue();
    }

    @Test
    void createFailsWhenEmailAlreadyExists() {
        when(users.existsByEmail("usuario@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateCommand("usuario@test.com", "x", "Otro", Role.USER)))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(users, never()).save(any());
    }

    @Test
    void updateChangesProfileButKeepsEmailAndPassword() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, Role.USER, true)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.update(new UpdateCommand(USER_ID, "Nombre Nuevo", Role.ADMIN, false, ADMIN_ID));

        assertThat(updated.fullName()).isEqualTo("Nombre Nuevo");
        assertThat(updated.role()).isEqualTo(Role.ADMIN);
        assertThat(updated.active()).isFalse();
        assertThat(updated.email()).isEqualTo("u2@test.com");
        assertThat(updated.passwordHash()).isEqualTo("hash");
    }

    @Test
    void updateFailsWhenUserDoesNotExist() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateCommand(99L, "X", Role.USER, true, ADMIN_ID)))
                .isInstanceOf(UserNotFoundException.class);
        verify(users, never()).save(any());
    }

    @Test
    void adminCannotDeactivateOrDemoteThemselves() {
        when(users.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, Role.ADMIN, true)));

        assertThatThrownBy(() -> service.update(new UpdateCommand(ADMIN_ID, "Admin", Role.ADMIN, false, ADMIN_ID)))
                .isInstanceOf(SelfModificationNotAllowedException.class);
        assertThatThrownBy(() -> service.update(new UpdateCommand(ADMIN_ID, "Admin", Role.USER, true, ADMIN_ID)))
                .isInstanceOf(SelfModificationNotAllowedException.class);
        verify(users, never()).save(any());
    }

    @Test
    void adminCanRenameThemselves() {
        when(users.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, Role.ADMIN, true)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.update(new UpdateCommand(ADMIN_ID, "Otro Nombre", Role.ADMIN, true, ADMIN_ID));

        assertThat(updated.fullName()).isEqualTo("Otro Nombre");
    }

    @Test
    void deleteIsALogicalDeactivation() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, Role.USER, true)));

        service.delete(USER_ID, ADMIN_ID);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().active()).isFalse();
        verify(users, never()).deleteById(any());
    }

    @Test
    void deleteIsIdempotentForAnAlreadyInactiveUser() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, Role.USER, false)));

        service.delete(USER_ID, ADMIN_ID);

        verify(users, never()).save(any());
    }

    @Test
    void adminCannotDeleteThemselves() {
        when(users.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, Role.ADMIN, true)));

        assertThatThrownBy(() -> service.delete(ADMIN_ID, ADMIN_ID))
                .isInstanceOf(SelfModificationNotAllowedException.class);
        verify(users, never()).save(any());
    }

    @Test
    void deleteFailsWhenUserDoesNotExist() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, ADMIN_ID)).isInstanceOf(UserNotFoundException.class);
    }

    private static User user(Long id, Role role, boolean active) {
        return new User(id, "u" + id + "@test.com", "hash", "Usuario " + id, role, active, NOW);
    }
}
