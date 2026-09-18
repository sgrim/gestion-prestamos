package com.makers.prestamos.infrastructure.adapter.out.notification;

import com.makers.prestamos.application.port.out.LoanNotificationPort;
import com.makers.prestamos.domain.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/** Notificación por defecto: solo registra la decisión. Se activa cuando no hay webhook configurado. */
@Component
@ConditionalOnExpression("'${app.notifications.webhook-url:}'.isEmpty()")
class LoggingLoanNotificationAdapter implements LoanNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingLoanNotificationAdapter.class);

    @Override
    public void loanDecided(Loan loan) {
        log.info("Préstamo {} resuelto: {} (por admin {})", loan.id(), loan.status(), loan.decidedBy());
    }
}
