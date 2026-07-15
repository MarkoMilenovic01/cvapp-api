package com.best.cvapp.auth.emailverification.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;


public class InvalidVerificationTokenException extends AppException {
    public InvalidVerificationTokenException() {
        super(HttpStatus.BAD_REQUEST, "Invalid verification token");
    }
}