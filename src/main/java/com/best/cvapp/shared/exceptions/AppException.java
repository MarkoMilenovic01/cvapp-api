package com.best.cvapp.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final Object details;

    protected AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.details = null;
    }

    protected AppException(HttpStatus status, String message, Object details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}