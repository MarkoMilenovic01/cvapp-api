package com.best.cvapp.auth.oauth.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class OAuthAccountConflictException extends AppException {
    public OAuthAccountConflictException() {
        super(HttpStatus.CONFLICT, "An account with this email already exists using a different login method");
    }
}