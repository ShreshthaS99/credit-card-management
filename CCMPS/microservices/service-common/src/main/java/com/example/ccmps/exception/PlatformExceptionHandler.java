package com.example.ccmps.exception;

import feign.FeignException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class PlatformExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(PlatformExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> missing(ResourceNotFoundException ex) {
        return error(404, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> unknownRoute(NoResourceFoundException ex) {
        return error(404, "RESOURCE_NOT_FOUND", "Endpoint not found.");
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(BusinessException ex) {
        return error(400, "BUSINESS_ERROR", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(AccessDeniedException ex) {
        return error(403, "FORBIDDEN", "You do not have access to this resource.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> unauthenticated(AuthenticationException ex) {
        return error(401, "UNAUTHORIZED", "Invalid credentials.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.unprocessableEntity().body(new ApiError(
                422, "VALIDATION_FAILED", "Please correct the invalid fields.",
                LocalDateTime.now(), fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> malformed(Exception ex) {
        return error(400, "INVALID_REQUEST", "Invalid request value or JSON body.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex) {
        return error(409, "CONFLICT", "The request conflicts with an existing record.");
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ApiError> routing(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        return error(status, status == 503 ? "DEPENDENCY_UNAVAILABLE" : "UPSTREAM_ERROR",
                "The upstream service could not complete the request.");
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<ApiError> unreachable(ResourceAccessException ex) {
        log.warn("Upstream connection failed: {}", ex.getClass().getSimpleName());
        return error(503, "DEPENDENCY_UNAVAILABLE", "The upstream service is unavailable.");
    }

    @ExceptionHandler({FeignException.class, NoFallbackAvailableException.class})
    ResponseEntity<ApiError> remote(RuntimeException ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && !(cause instanceof FeignException)) {
            cause = cause.getCause();
        }
        if (cause instanceof FeignException feign && feign.status() >= 400
                && feign.status() < 500) {
            return error(feign.status(), "REMOTE_REQUEST_REJECTED",
                    "A dependent service rejected the request.");
        }
        log.warn("Dependent service unavailable: {}", cause.getClass().getSimpleName());
        return error(503, "DEPENDENCY_UNAVAILABLE",
                "A required service is unavailable. Retry later using the same request details.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unhandled service error", ex);
        return error(500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
    }

    private ResponseEntity<ApiError> error(int status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(status, code, message, LocalDateTime.now(), Map.of()));
    }
}
