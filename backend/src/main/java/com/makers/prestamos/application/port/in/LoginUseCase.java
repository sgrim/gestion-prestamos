package com.makers.prestamos.application.port.in;

import com.makers.prestamos.application.port.out.TokenIssuerPort.IssuedToken;
import com.makers.prestamos.domain.model.User;

public interface LoginUseCase {

    Result login(String email, String rawPassword);

    record Result(IssuedToken token, User user) {
    }
}
