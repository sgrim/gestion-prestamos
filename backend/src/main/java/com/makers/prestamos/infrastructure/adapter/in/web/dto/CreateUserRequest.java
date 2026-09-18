package com.makers.prestamos.infrastructure.adapter.in.web.dto;

import com.makers.prestamos.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 72) String password,
        @NotBlank @Size(max = 150) String fullName,
        @NotNull Role role
) {
}
