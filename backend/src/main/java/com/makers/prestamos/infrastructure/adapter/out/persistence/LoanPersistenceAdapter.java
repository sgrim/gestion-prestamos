package com.makers.prestamos.infrastructure.adapter.out.persistence;

import com.makers.prestamos.application.port.out.LoanRepositoryPort;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import com.makers.prestamos.infrastructure.adapter.out.persistence.repository.SpringDataLoanRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.makers.prestamos.infrastructure.config.CacheConfig.LOANS_BY_ID;
import static com.makers.prestamos.infrastructure.config.CacheConfig.LOANS_BY_USER;

/**
 * La caché vive aquí (y no en el servicio de aplicación) porque es un detalle de
 * infraestructura: el dominio y los casos de uso no saben que existe.
 */
@Component
class LoanPersistenceAdapter implements LoanRepositoryPort {

    private final SpringDataLoanRepository repository;

    LoanPersistenceAdapter(SpringDataLoanRepository repository) {
        this.repository = repository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = LOANS_BY_USER, key = "#loan.userId()"),
            @CacheEvict(cacheNames = LOANS_BY_ID, key = "#loan.id()", condition = "#loan.id() != null")
    })
    public Loan save(Loan loan) {
        return PersistenceMapper.toDomain(repository.save(PersistenceMapper.toEntity(loan)));
    }

    @Override
    @Cacheable(cacheNames = LOANS_BY_ID, key = "#id")
    public Optional<Loan> findById(Long id) {
        return repository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Loan> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(PersistenceMapper::toDomain);
    }

    @Override
    @Cacheable(cacheNames = LOANS_BY_USER, key = "#userId")
    public List<Loan> findByUserId(Long userId) {
        return repository.findByUserIdOrderByRequestedAtDescIdDesc(userId).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Loan> findAll(LoanStatus status) {
        var entities = status == null
                ? repository.findAllByOrderByRequestedAtDescIdDesc()
                : repository.findByStatusOrderByRequestedAtDescIdDesc(status);
        return entities.stream().map(PersistenceMapper::toDomain).toList();
    }
}
