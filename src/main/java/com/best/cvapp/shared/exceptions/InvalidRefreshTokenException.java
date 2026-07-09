package com.best.cvapp.shared.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AppException {

    public InvalidRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
}