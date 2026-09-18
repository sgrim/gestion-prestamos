package com.makers.prestamos.infrastructure.adapter.out.persistence;

import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.domain.model.User;
import com.makers.prestamos.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return PersistenceMapper.toDomain(repository.save(PersistenceMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(PersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
