package com.makers.prestamos.application.service;

import com.makers.prestamos.application.port.in.LoginUseCase;
import com.makers.prestamos.application.port.out.PasswordHasherPort;
import com.makers.prestamos.application.port.out.TokenIssuerPort;
import com.makers.prestamos.application.port.out.UserRepositoryPort;
import com.makers.prestamos.domain.exception.InvalidCredentialsException;
import com.makers.prestamos.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokens;

    public LoginService(UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public Result login(String email, String rawPassword) {
        User user = users.findByEmail(email.trim().toLowerCase())
                .filter(User::active)
                .filter(candidate -> hasher.matches(rawPassword, candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return new Result(tokens.issue(user), user);
    }
}
