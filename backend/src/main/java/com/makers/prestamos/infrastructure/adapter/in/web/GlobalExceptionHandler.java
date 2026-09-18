package com.makers.prestamos.infrastructure.adapter.in.web;

import com.makers.prestamos.domain.exception.EmailAlreadyRegisteredException;
import com.makers.prestamos.domain.exception.InvalidCredentialsException;
import com.makers.prestamos.domain.exception.InvalidLoanException;
import com.makers.prestamos.domain.exception.InvalidUserException;
import com.makers.prestamos.domain.exception.LoanAccessDeniedException;
import com.makers.prestamos.domain.exception.LoanAlreadyDecidedException;
import com.makers.prestamos.domain.exception.LoanNotFoundException;
import com.makers.prestamos.domain.exception.SelfModificationNotAllowedException;
import com.makers.prestamos.domain.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas HTTP claras con formato RFC 9457 (ProblemDetail).
 * Hereda de {@link ResponseEntityExceptionHandler}, que ya resuelve las excepciones estándar de
 * Spring MVC (JSON mal formado, tipo de parámetro incorrecto, ruta inexistente, método no permitido...).
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({LoanNotFoundException.class, UserNotFoundException.class})
    ResponseEntity<Object> notFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler({LoanAlreadyDecidedException.class, EmailAlreadyRegisteredException.class,
            SelfModificationNotAllowedException.class})
    ResponseEntity<Object> conflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage());
    }

    @ExceptionHandler({InvalidLoanException.class, InvalidUserException.class})
    ResponseEntity<Object> businessRule(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<Object> invalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    /** Cubre tanto el dominio ({@code LoanAccessDeniedException}) como {@code @PreAuthorize}. */
    @ExceptionHandler({LoanAccessDeniedException.class, AccessDeniedException.class})
    ResponseEntity<Object> forbidden(RuntimeException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permisos para realizar esta acción");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La petición contiene datos inválidos");
        detail.setTitle("Validación fallida");
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    private static ResponseEntity<Object> problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return ResponseEntity.status(status).body(problem);
    }
}
