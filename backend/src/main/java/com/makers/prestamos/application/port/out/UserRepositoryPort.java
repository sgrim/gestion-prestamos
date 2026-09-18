package com.makers.prestamos.application.port.out;

import com.makers.prestamos.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAll();

    void deleteById(Long id);
}
