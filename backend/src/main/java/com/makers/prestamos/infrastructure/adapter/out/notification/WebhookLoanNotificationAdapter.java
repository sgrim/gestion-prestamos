package com.makers.prestamos.infrastructure.adapter.out.notification;

import com.makers.prestamos.application.port.out.LoanNotificationPort;
import com.makers.prestamos.domain.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Único punto donde se usa Spring WebFlux: una llamada saliente no bloqueante.
 * Aquí sí aporta valor (I/O de red hacia un tercero que no debe frenar ni tumbar la aprobación),
 * mientras que el resto de la API es CRUD sobre JPA, que es bloqueante por naturaleza.
 *
 * <p>Se ejecuta después del commit: si la transacción se revierte, no se notifica nada.
 */
@Component
@ConditionalOnExpression("!'${app.notifications.webhook-url:}'.isEmpty()")
class WebhookLoanNotificationAdapter implements LoanNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookLoanNotificationAdapter.class);

    private final WebClient client;

    WebhookLoanNotificationAdapter(WebClient.Builder builder, @Value("${app.notifications.webhook-url}") String url) {
        this.client = builder.baseUrl(url).build();
    }

    @Override
    public void loanDecided(Loan loan) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(loan);
                }
            });
        } else {
            send(loan);
        }
    }

    private void send(Loan loan) {
        Map<String, Object> body = Map.of(
                "loanId", loan.id(),
                "userId", loan.userId(),
                "status", loan.status().name(),
                "amount", loan.amount());

        client.post()
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        response -> log.debug("Webhook entregado para el préstamo {}", loan.id()),
                        error -> log.warn("No se pudo notificar el préstamo {}: {}", loan.id(), error.getMessage()));
    }
}
