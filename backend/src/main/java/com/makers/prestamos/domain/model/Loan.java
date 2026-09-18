package com.makers.prestamos.domain.model;

import com.makers.prestamos.domain.exception.InvalidLoanException;
import com.makers.prestamos.domain.exception.LoanAlreadyDecidedException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Agregado raíz de préstamo. Es inmutable: cada transición devuelve una instancia nueva,
 * lo que además lo hace seguro para guardarse en caché.
 *
 * <p>Reglas de negocio:
 * <ul>
 *   <li>Monto entre {@value #MIN_AMOUNT_TEXT} y {@value #MAX_AMOUNT_TEXT}.</li>
 *   <li>Plazo entre {@value #MIN_TERM_MONTHS} y {@value #MAX_TERM_MONTHS} meses.</li>
 *   <li>Una solicitud nace en {@code PENDING} y solo puede decidirse una vez.</li>
 * </ul>
 */
public record Loan(
        Long id,
        Long userId,
        BigDecimal amount,
        int termMonths,
        LoanStatus status,
        Instant requestedAt,
        Instant decidedAt,
        Long decidedBy
) implements Serializable {

    public static final String MIN_AMOUNT_TEXT = "100";
    public static final String MAX_AMOUNT_TEXT = "1000000";
    public static final int MIN_TERM_MONTHS = 1;
    public static final int MAX_TERM_MONTHS = 84;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal(MIN_AMOUNT_TEXT);
    private static final BigDecimal MAX_AMOUNT = new BigDecimal(MAX_AMOUNT_TEXT);

    public static Loan request(Long userId, BigDecimal amount, int termMonths, Instant now) {
        if (userId == null) {
            throw new InvalidLoanException("El préstamo debe pertenecer a un usuario");
        }
        if (amount == null) {
            throw new InvalidLoanException("El monto es obligatorio");
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(MIN_AMOUNT) < 0 || normalized.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidLoanException(
                    "El monto debe estar entre %s y %s".formatted(MIN_AMOUNT_TEXT, MAX_AMOUNT_TEXT));
        }
        if (termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
            throw new InvalidLoanException(
                    "El plazo debe estar entre %d y %d meses".formatted(MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
        return new Loan(null, userId, normalized, termMonths, LoanStatus.PENDING, now, null, null);
    }

    public Loan approve(Long adminId, Instant now) {
        return decide(LoanStatus.APPROVED, adminId, now);
    }

    public Loan reject(Long adminId, Instant now) {
        return decide(LoanStatus.REJECTED, adminId, now);
    }

    public boolean belongsTo(Long candidateUserId) {
        return userId.equals(candidateUserId);
    }

    private Loan decide(LoanStatus newStatus, Long adminId, Instant now) {
        if (!status.isPending()) {
            throw new LoanAlreadyDecidedException(id, status);
        }
        return new Loan(id, userId, amount, termMonths, newStatus, requestedAt, now, adminId);
    }
}
