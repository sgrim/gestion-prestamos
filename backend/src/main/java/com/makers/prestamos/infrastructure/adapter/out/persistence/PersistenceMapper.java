package com.makers.prestamos.infrastructure.adapter.out.persistence;

import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.User;
import com.makers.prestamos.infrastructure.adapter.out.persistence.entity.LoanEntity;
import com.makers.prestamos.infrastructure.adapter.out.persistence.entity.UserEntity;

/** Traduce entre el modelo de dominio y las entidades JPA. */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static Loan toDomain(LoanEntity e) {
        return new Loan(e.getId(), e.getUserId(), e.getAmount(), e.getTermMonths(), e.getStatus(),
                e.getRequestedAt(), e.getDecidedAt(), e.getDecidedBy());
    }

    static LoanEntity toEntity(Loan l) {
        return new LoanEntity(l.id(), l.userId(), l.amount(), l.termMonths(), l.status(),
                l.requestedAt(), l.decidedAt(), l.decidedBy());
    }

    static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.getFullName(),
                e.getRole(), e.isActive(), e.getCreatedAt());
    }

    static UserEntity toEntity(User u) {
        return new UserEntity(u.id(), u.email(), u.passwordHash(), u.fullName(),
                u.role(), u.active(), u.createdAt());
    }
}
