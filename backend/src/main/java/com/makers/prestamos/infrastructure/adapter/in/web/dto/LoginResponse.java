package com.makers.prestamos.infrastructure.adapter.in.web.dto;

import com.makers.prestamos.application.port.in.LoginUseCase;

import java.time.Instant;

public record LoginResponse(String token, String tokenType, Instant expiresAt, UserResponse user) {

    public static LoginResponse from(LoginUseCase.Result result) {
        return new LoginResponse(
                result.token().value(), "Bearer", result.token().expiresAt(), UserResponse.from(result.user()));
    }
}
