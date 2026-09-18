package com.makers.prestamos.application;

import com.makers.prestamos.application.port.in.RequestLoanUseCase.Command;
import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.application.service.RequestLoanService;
import com.makers.prestamos.domain.exception.InvalidLoanException;
import com.makers.prestamos.domain.exception.UserNotFoundException;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import com.makers.prestamos.domain.model.Role;
import com.makers.prestamos.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class RequestLoanServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock LoanRepositoryPort loans;
    @Mock UserRepositoryPort users;

    RequestLoanService service;

    @BeforeEach
    void setUp() {
        service = new RequestLoanService(loans, users, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesAPendingLoanForTheUser() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(loans.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        service.requestLoan(new Command(1L, new BigDecimal("5000"), 24));

        ArgumentCaptor<Loan> saved = ArgumentCaptor.forClass(Loan.class);
        verify(loans).save(saved.capture());
        assertThat(saved.getValue().userId()).isEqualTo(1L);
        assertThat(saved.getValue().status()).isEqualTo(LoanStatus.PENDING);
        assertThat(saved.getValue().requestedAt()).isEqualTo(NOW);
    }

    @Test
    void failsWhenUserDoesNotExist() {
        when(users.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestLoan(new Command(42L, new BigDecimal("5000"), 24)))
                .isInstanceOf(UserNotFoundException.class);
        verify(loans, never()).save(any());
    }

    @Test
    void doesNotPersistWhenBusinessRulesFail() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));

        assertThatThrownBy(() -> service.requestLoan(new Command(1L, new BigDecimal("10"), 24)))
                .isInstanceOf(InvalidLoanException.class);
        verify(loans, never()).save(any());
    }

    private static User user(Long id) {
        return new User(id, "usuario@test.com", "hash", "Usuario", Role.USER, true, NOW);
    }
}
