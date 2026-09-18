package com.makers.prestamos.domain;

import com.makers.prestamos.domain.exception.InvalidLoanException;
import com.makers.prestamos.domain.exception.LoanAlreadyDecidedException;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    @DisplayName("una solicitud válida nace pendiente y sin decisión")
    void requestCreatesPendingLoan() {
        Loan loan = Loan.request(1L, new BigDecimal("1000"), 12, NOW);

        assertThat(loan.status()).isEqualTo(LoanStatus.PENDING);
        assertThat(loan.amount()).isEqualByComparingTo("1000.00");
        assertThat(loan.decidedAt()).isNull();
        assertThat(loan.decidedBy()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "99.99", "1000000.01"})
    @DisplayName("rechaza montos fuera del rango permitido")
    void rejectsInvalidAmounts(String amount) {
        assertThatThrownBy(() -> Loan.request(1L, new BigDecimal(amount), 12, NOW))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("monto");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 85})
    @DisplayName("rechaza plazos fuera del rango permitido")
    void rejectsInvalidTerms(int months) {
        assertThatThrownBy(() -> Loan.request(1L, new BigDecimal("1000"), months, NOW))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("plazo");
    }

    @Test
    @DisplayName("aprobar registra estado, fecha y administrador sin mutar el original")
    void approveReturnsNewInstance() {
        Loan pending = new Loan(7L, 1L, new BigDecimal("1000.00"), 12, LoanStatus.PENDING, NOW, null, null);
        Instant later = NOW.plusSeconds(60);

        Loan approved = pending.approve(99L, later);

        assertThat(approved.status()).isEqualTo(LoanStatus.APPROVED);
        assertThat(approved.decidedBy()).isEqualTo(99L);
        assertThat(approved.decidedAt()).isEqualTo(later);
        assertThat(pending.status()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    @DisplayName("un préstamo ya resuelto no puede volver a decidirse")
    void cannotDecideTwice() {
        Loan approved = new Loan(7L, 1L, new BigDecimal("1000.00"), 12, LoanStatus.APPROVED, NOW, NOW, 99L);

        assertThatThrownBy(() -> approved.reject(99L, NOW))
                .isInstanceOf(LoanAlreadyDecidedException.class);
    }
}
