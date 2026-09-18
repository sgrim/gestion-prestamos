package com.makers.prestamos.infrastructure.adapter.out.persistence.repository;

import com.makers.prestamos.domain.model.LoanStatus;
import com.makers.prestamos.infrastructure.adapter.out.persistence.entity.LoanEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataLoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByUserIdOrderByRequestedAtDescIdDesc(Long userId);

    List<LoanEntity> findByStatusOrderByRequestedAtDescIdDesc(LoanStatus status);

    List<LoanEntity> findAllByOrderByRequestedAtDescIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanEntity l where l.id = :id")
    Optional<LoanEntity> findByIdForUpdate(@Param("id") Long id);
}
