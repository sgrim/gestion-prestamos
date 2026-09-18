package com.makers.prestamos.infrastructure.adapter.out.persistence.entity;

import com.makers.prestamos.domain.model.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loans")
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decided_by")
    private Long decidedBy;

    protected LoanEntity() {
        // requerido por JPA
    }

    public LoanEntity(Long id, Long userId, BigDecimal amount, int termMonths, LoanStatus status,
                      Instant requestedAt, Instant decidedAt, Long decidedBy) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.termMonths = termMonths;
        this.status = status;
        this.requestedAt = requestedAt;
        this.decidedAt = decidedAt;
        this.decidedBy = decidedBy;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public int getTermMonths() { return termMonths; }
    public LoanStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public Long getDecidedBy() { return decidedBy; }
}
