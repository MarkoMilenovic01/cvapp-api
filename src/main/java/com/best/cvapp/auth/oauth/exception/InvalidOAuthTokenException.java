package com.best.cvapp.auth.oauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidOAuthTokenException extends RuntimeException {

    public InvalidOAuthTokenException() {
        super("Invalid or expired Google ID token");
    }
}