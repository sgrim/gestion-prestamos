package com.makers.prestamos.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        /** Clave HMAC-SHA256: mínimo 32 caracteres. Se inyecta por variable de entorno. */
        @NotBlank @Size(min = 32) String secret,
        @NotNull Duration expiration,
        @NotBlank String issuer
) {
}
