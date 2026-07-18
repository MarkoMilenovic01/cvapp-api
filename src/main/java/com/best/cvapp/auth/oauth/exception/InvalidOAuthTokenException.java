package com.best.cvapp.auth.oauth.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidOAuthTokenException extends AppException {

    public InvalidOAuthTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid or expired Google ID token");
    }
}
