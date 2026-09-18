package com.makers.prestamos.application.service;

import com.makers.prestamos.application.port.in.DecideLoanUseCase;
import com.makers.prestamos.application.port.out.LoanNotificationPort;
import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.domain.exception.LoanNotFoundException;
import com.makers.prestamos.domain.model.Loan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.function.BiFunction;

/**
 * Aprobar/rechazar es una operación transaccional: se bloquea la fila, se valida el estado
 * y se persiste la decisión de forma atómica. Si algo falla, no queda ningún cambio a medias.
 */
@Service
@Transactional
public class DecideLoanService implements DecideLoanUseCase {

    private final LoanRepositoryPort loans;
    private final LoanNotificationPort notifications;
    private final Clock clock;

    public DecideLoanService(LoanRepositoryPort loans, LoanNotificationPort notifications, Clock clock) {
        this.loans = loans;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Override
    public Loan approve(Long loanId, Long adminId) {
        return decide(loanId, adminId, Loan::approve);
    }

    @Override
    public Loan reject(Long loanId, Long adminId) {
        return decide(loanId, adminId, Loan::reject);
    }

    private Loan decide(Long loanId, Long adminId, DecisionRule rule) {
        Loan loan = loans.findByIdForUpdate(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        Loan decided = loans.save(rule.apply(loan, adminId, clock.instant()));
        notifications.loanDecided(decided);
        return decided;
    }

    @FunctionalInterface
    private interface DecisionRule {
        Loan apply(Loan loan, Long adminId, java.time.Instant now);
    }
}
