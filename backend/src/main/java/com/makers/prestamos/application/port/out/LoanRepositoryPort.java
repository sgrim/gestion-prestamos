package com.makers.prestamos.application.port.out;

import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;

import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {

    Loan save(Loan loan);

    Optional<Loan> findById(Long id);

    /** Lectura con bloqueo de escritura: evita que dos administradores decidan el mismo préstamo a la vez. */
    Optional<Loan> findByIdForUpdate(Long id);

    List<Loan> findByUserId(Long userId);

    /** @param status filtro opcional; {@code null} devuelve todos. */
    List<Loan> findAll(LoanStatus status);
}
