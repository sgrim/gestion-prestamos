package com.makers.prestamos.application.service;

import com.makers.prestamos.application.port.in.LoanQueryUseCase;
import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.domain.exception.LoanAccessDeniedException;
import com.makers.prestamos.domain.exception.LoanNotFoundException;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LoanQueryService implements LoanQueryUseCase {

    private final LoanRepositoryPort loans;

    public LoanQueryService(LoanRepositoryPort loans) {
        this.loans = loans;
    }

    @Override
    public Loan getLoan(Long loanId, Long requesterId, boolean requesterIsAdmin) {
        Loan loan = loans.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        if (!requesterIsAdmin && !loan.belongsTo(requesterId)) {
            throw new LoanAccessDeniedException(loanId);
        }
        return loan;
    }

    @Override
    public List<Loan> listByUser(Long userId) {
        return loans.findByUserId(userId);
    }

    @Override
    public List<Loan> listAll(LoanStatus status) {
        return loans.findAll(status);
    }
}
