package com.best.cvapp.shared.exceptions;

import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends AppException {

    public RefreshTokenExpiredException() {
        super(HttpStatus.UNAUTHORIZED, "Refresh token expired");
    }
}