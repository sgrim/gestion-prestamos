package com.makers.prestamos.infrastructure.adapter.in.web;

import com.makers.prestamos.application.port.in.LoginUseCase;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.LoginRequest;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final LoginUseCase login;

    AuthController(LoginUseCase login) {
        this.login = login;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(login.login(request.email(), request.password()));
    }
}
