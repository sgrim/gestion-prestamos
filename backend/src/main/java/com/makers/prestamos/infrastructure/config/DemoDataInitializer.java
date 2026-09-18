package com.makers.prestamos.infrastructure.config;

import com.makers.prestamos.application.port.in.DecideLoanUseCase;
import com.makers.prestamos.application.port.in.ManageUsersUseCase;
import com.makers.prestamos.application.port.in.RequestLoanUseCase;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga los usuarios y préstamos de ejemplo de la especificación (usuario@test.com / admin@test.com).
 * Solo actúa si la base de datos está vacía y {@code app.seed.enabled=true}. Desactívalo en producción.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final ManageUsersUseCase users;
    private final RequestLoanUseCase requestLoan;
    private final DecideLoanUseCase decideLoan;
    private final String demoPassword;

    DemoDataInitializer(ManageUsersUseCase users, RequestLoanUseCase requestLoan, DecideLoanUseCase decideLoan,
                        @Value("${app.seed.password}") String demoPassword) {
        this.users = users;
        this.requestLoan = requestLoan;
        this.decideLoan = decideLoan;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!users.listAll().isEmpty()) {
            return;
        }
        var admin = users.create(new ManageUsersUseCase.CreateCommand(
                "admin@test.com", demoPassword, "Admin Admin", Role.ADMIN));
        var user = users.create(new ManageUsersUseCase.CreateCommand(
                "usuario@test.com", demoPassword, "Usuario Demo", Role.USER));

        request(user.id(), "1000", 12);
        Loan approved = request(user.id(), "2000", 24);
        request(user.id(), "30000", 60);
        decideLoan.approve(approved.id(), admin.id());

        log.info("Datos de demostración cargados: admin@test.com y usuario@test.com");
    }

    private Loan request(Long userId, String amount, int termMonths) {
        return requestLoan.requestLoan(new RequestLoanUseCase.Command(userId, new BigDecimal(amount), termMonths));
    }
}
