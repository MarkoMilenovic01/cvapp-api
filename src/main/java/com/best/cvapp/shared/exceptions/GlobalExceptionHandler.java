package com.best.cvapp.shared.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles custom application exceptions.
     *
     * Examples:
     * - EmailAlreadyInUseException
     * - PasswordsDoNotMatchException
     * - GoogleAccountLoginRequiredException
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(buildResponse(
                ex.getStatus(),
                ex.getMessage(),
                ex.getDetails()
        ));
    }

    /**
     * Handles @Valid request body validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        ));
    }

    /**
     * Handles validation failures from request params/path variables.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        });

        return ResponseEntity.badRequest().body(buildResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                errors
        ));
    }

    /**
     * Handles wrong email/password.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                null
        ));
    }

    /**
     * Handles forbidden access.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied — you don't have permission to access this resource",
                null
        ));
    }

    /**
     * Handles ResponseStatusException.
     *
     * You can keep this while you still have old services/controllers
     * throwing ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return ResponseEntity.status(status).body(buildResponse(
                status,
                ex.getReason(),
                null
        ));
    }

    /**
     * Handles too-large uploaded files.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File is too large. Maximum allowed size is 10 MB",
                null
        ));
    }

    /**
     * Handles missing multipart file.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestPart(
            MissingServletRequestPartException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildResponse(
                HttpStatus.BAD_REQUEST,
                "Required file is missing",
                null
        ));
    }

    /**
     * Handles simple business validation errors.
     *
     * Example:
     * - wrong file type
     * - invalid Cloudinary upload input
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        ));
    }

    /**
     * Catch-all for unexpected errors.
     *
     * Keep this last.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
        ));
    }

    private Map<String, Object> buildResponse(HttpStatus status, String message, Object details) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        if (details != null) {
            response.put("details", details);
        }

        return response;
    }
}