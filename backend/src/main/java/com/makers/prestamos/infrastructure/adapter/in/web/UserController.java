package com.makers.prestamos.infrastructure.adapter.in.web;

import com.makers.prestamos.application.port.in.ManageUsersUseCase;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** CRUD de usuarios: solo administradores. */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController {

    private final ManageUsersUseCase users;

    UserController(ManageUsersUseCase users) {
        this.users = users;
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body) {
        var created = users.create(
                new ManageUsersUseCase.CreateCommand(body.email(), body.password(), body.fullName(), body.role()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(created));
    }

    @GetMapping
    List<UserResponse> list() {
        return users.listAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    UserResponse get(@PathVariable Long id) {
        return UserResponse.from(users.getById(id));
    }

    @PutMapping("/{id}")
    UserResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                        @Valid @RequestBody UpdateUserRequest body) {
        return UserResponse.from(users.update(new ManageUsersUseCase.UpdateCommand(
                id, body.fullName(), body.role(), body.active(), CurrentUser.from(jwt).id())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        users.delete(id, CurrentUser.from(jwt).id());
    }
}
