package com.makers.prestamos.infrastructure.adapter.in.web;

import com.makers.prestamos.application.port.in.DecideLoanUseCase;
import com.makers.prestamos.application.port.in.LoanQueryUseCase;
import com.makers.prestamos.application.port.in.ManageUsersUseCase;
import com.makers.prestamos.application.port.in.RequestLoanUseCase;
import com.makers.prestamos.domain.model.Loan;
import com.makers.prestamos.domain.model.LoanStatus;
import com.makers.prestamos.domain.model.User;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.LoanResponse;
import com.makers.prestamos.infrastructure.adapter.in.web.dto.RequestLoanRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
class LoanController {

    private final RequestLoanUseCase requestLoan;
    private final DecideLoanUseCase decideLoan;
    private final LoanQueryUseCase queries;
    private final ManageUsersUseCase users;

    LoanController(RequestLoanUseCase requestLoan, DecideLoanUseCase decideLoan,
                   LoanQueryUseCase queries, ManageUsersUseCase users) {
        this.requestLoan = requestLoan;
        this.decideLoan = decideLoan;
        this.queries = queries;
        this.users = users;
    }

    @PostMapping
    ResponseEntity<LoanResponse> request(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RequestLoanRequest body) {
        CurrentUser me = CurrentUser.from(jwt);
        Loan loan = requestLoan.requestLoan(
                new RequestLoanUseCase.Command(me.id(), body.amount(), body.termMonths()));

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(loan.id()).toUri();
        return ResponseEntity.created(location).body(toResponse(loan));
    }

    /** Estado de los préstamos del usuario autenticado. */
    @GetMapping("/me")
    List<LoanResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser me = CurrentUser.from(jwt);
        return queries.listByUser(me.id()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    LoanResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        CurrentUser me = CurrentUser.from(jwt);
        return toResponse(queries.getLoan(id, me.id(), me.admin()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    List<LoanResponse> all(@RequestParam(required = false) LoanStatus status) {
        List<Loan> loans = queries.listAll(status);
        Map<Long, String> emails = emailsById();
        return loans.stream()
                .map(loan -> LoanResponse.from(loan, emails.get(loan.userId())))
                .toList();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    LoanResponse approve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return toResponse(decideLoan.approve(id, CurrentUser.from(jwt).id()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    LoanResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return toResponse(decideLoan.reject(id, CurrentUser.from(jwt).id()));
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.from(loan, users.getById(loan.userId()).email());
    }

    /** Evita consultar el usuario de cada fila (N+1) al listar. */
    private Map<Long, String> emailsById() {
        return users.listAll().stream()
                .collect(Collectors.toMap(User::id, User::email, (a, b) -> a, HashMap::new));
    }
}
