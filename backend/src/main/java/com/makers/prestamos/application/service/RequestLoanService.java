package com.makers.prestamos.application.service;

import com.makers.prestamos.application.port.in.RequestLoanUseCase;
import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.domain.exception.UserNotFoundException;
import com.makers.prestamos.domain.model.Loan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@Transactional
public class RequestLoanService implements RequestLoanUseCase {

    private final LoanRepositoryPort loans;
    private final UserRepositoryPort users;
    private final Clock clock;

    public RequestLoanService(LoanRepositoryPort loans, UserRepositoryPort users, Clock clock) {
        this.loans = loans;
        this.users = users;
        this.clock = clock;
    }

    @Override
    public Loan requestLoan(Command command) {
        users.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Loan loan = Loan.request(command.userId(), command.amount(), command.termMonths(), clock.instant());
        return loans.save(loan);
    }
}
