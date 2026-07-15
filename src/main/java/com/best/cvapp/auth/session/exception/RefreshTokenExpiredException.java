package com.best.cvapp.auth.session.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends AppException {

    public RefreshTokenExpiredException() {
        super(HttpStatus.UNAUTHORIZED, "Refresh token expired");
    }
}