package com.makers.prestamos.infrastructure.adapter.out.security;

import com.makers.prestamos.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder encoder;

    BcryptPasswordHasherAdapter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
