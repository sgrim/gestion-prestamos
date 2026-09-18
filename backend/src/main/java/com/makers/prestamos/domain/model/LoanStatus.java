package com.makers.prestamos.domain.model;

public enum LoanStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public boolean isPending() {
        return this == PENDING;
    }
}
