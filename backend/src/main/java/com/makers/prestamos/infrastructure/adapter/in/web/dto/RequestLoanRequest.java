package com.makers.prestamos.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Valida la forma de la entrada. Los límites de negocio (monto y plazo permitidos)
 * viven en el dominio ({@code Loan.request}) para que se apliquen venga de donde venga la petición.
 */
public record RequestLoanRequest(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal amount,

        @NotNull(message = "El plazo es obligatorio")
        @Positive(message = "El plazo debe ser mayor que cero")
        Integer termMonths
) {
}
