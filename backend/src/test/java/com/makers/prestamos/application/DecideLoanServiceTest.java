package com.makers.prestamos.application;

import com.makers.prestamos.application.port.out.LoanNotificationPort;
import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.application.service.DecideLoanService;
import com.makers.prestamos.domain.exception.LoanAlreadyDecidedException;
import com.makers.prestamos.domain.exception.LoanNotFoundException;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class DecideLoanServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Long ADMIN_ID = 99L;

    @Mock LoanRepositoryPort loans;
    @Mock LoanNotificationPort notifications;

    DecideLoanService service;

    @BeforeEach
    void setUp() {
        service = new DecideLoanService(loans, notifications, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void approvePersistsTheDecisionAndNotifies() {
        when(loans.findByIdForUpdate(7L)).thenReturn(Optional.of(pendingLoan()));
        when(loans.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = service.approve(7L, ADMIN_ID);

        assertThat(result.status()).isEqualTo(LoanStatus.APPROVED);
        assertThat(result.decidedBy()).isEqualTo(ADMIN_ID);
        assertThat(result.decidedAt()).isEqualTo(NOW);
        verify(loans).save(result);
        verify(notifications).loanDecided(result);
    }

    @Test
    void approveFailsWhenLoanDoesNotExist() {
        when(loans.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(7L, ADMIN_ID)).isInstanceOf(LoanNotFoundException.class);
        verify(loans, never()).save(any());
        verify(notifications, never()).loanDecided(any());
    }

    @Test
    void approveFailsWhenLoanWasAlreadyDecided() {
        Loan rejected = new Loan(7L, 1L, new BigDecimal("1000.00"), 12, LoanStatus.REJECTED, NOW, NOW, ADMIN_ID);
        when(loans.findByIdForUpdate(7L)).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> service.approve(7L, ADMIN_ID)).isInstanceOf(LoanAlreadyDecidedException.class);
        verify(loans, never()).save(any());
        verify(notifications, never()).loanDecided(any());
    }

    // ------------------------------------------------------------------------------------------
    // TODO(candidato): escribe estos tests tú mismo, siguiendo el patrón de los de arriba.
    // ------------------------------------------------------------------------------------------

    @Test
    @Disabled("TODO(candidato): rechazar guarda estado REJECTED, registra al admin y notifica")
    void rejectPersistsTheDecisionAndNotifies() {
        // Given: un préstamo pendiente que devuelve loans.findByIdForUpdate
        // When:  service.reject(7L, ADMIN_ID)
        // Then:  estado REJECTED, decidedBy == ADMIN_ID, se guardó y se notificó
    }

    @Test
    @Disabled("TODO(candidato): no se puede rechazar un préstamo ya aprobado")
    void rejectFailsWhenLoanWasAlreadyApproved() {
        // Then: LoanAlreadyDecidedException y no se llama a save ni a notifications
    }

    private static Loan pendingLoan() {
        return new Loan(7L, 1L, new BigDecimal("1000.00"), 12, LoanStatus.PENDING, NOW.minusSeconds(3600), null, null);
    }
}
