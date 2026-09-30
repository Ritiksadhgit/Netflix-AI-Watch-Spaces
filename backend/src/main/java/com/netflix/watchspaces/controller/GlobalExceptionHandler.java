package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.config.CorrelationIdFilter;
import com.netflix.watchspaces.domain.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(
            ResponseStatusException ex, ServerWebExchange exchange) {
        String corrId = getCorrelationId(exchange);
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = ErrorResponse.builder()
                .type("https://api.netflix-watchspaces.com/errors/" + status.name())
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(ex.getReason() != null ? ex.getReason() : ex.getMessage())
                .instance(exchange.getRequest().getPath().value())
                .code("ERR_" + status.name())
                .timestamp(Instant.now().toString())
                .correlationId(corrId)
                .build();

        return Mono.just(ResponseEntity.status(status).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        String corrId = getCorrelationId(exchange);
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse error = ErrorResponse.builder()
                .type("https://api.netflix-watchspaces.com/errors/VALIDATION_FAILED")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Input payload contains one or more invalid fields")
                .instance(exchange.getRequest().getPath().value())
                .code("ERR_VALIDATION_FAILED")
                .timestamp(Instant.now().toString())
                .correlationId(corrId)
                .validationErrors(fieldErrors)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex, ServerWebExchange exchange) {
        String corrId = getCorrelationId(exchange);
        ErrorResponse error = ErrorResponse.builder()
                .type("https://api.netflix-watchspaces.com/errors/FORBIDDEN")
                .title("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have required permissions to perform this action")
                .instance(exchange.getRequest().getPath().value())
                .code("ERR_FORBIDDEN")
                .timestamp(Instant.now().toString())
                .correlationId(corrId)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAuthException(
            AuthenticationException ex, ServerWebExchange exchange) {
        String corrId = getCorrelationId(exchange);
        ErrorResponse error = ErrorResponse.builder()
                .type("https://api.netflix-watchspaces.com/errors/UNAUTHORIZED")
                .title("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance(exchange.getRequest().getPath().value())
                .code("ERR_UNAUTHORIZED")
                .timestamp(Instant.now().toString())
                .correlationId(corrId)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        String corrId = getCorrelationId(exchange);
        ErrorResponse error = ErrorResponse.builder()
                .type("https://api.netflix-watchspaces.com/errors/INTERNAL_SERVER_ERROR")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected server error occurred: " + ex.getMessage())
                .instance(exchange.getRequest().getPath().value())
                .code("ERR_INTERNAL_ERROR")
                .timestamp(Instant.now().toString())
                .correlationId(corrId)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    private String getCorrelationId(ServerWebExchange exchange) {
        String corrId = exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        return corrId != null ? corrId : "NONE";
    }
}
