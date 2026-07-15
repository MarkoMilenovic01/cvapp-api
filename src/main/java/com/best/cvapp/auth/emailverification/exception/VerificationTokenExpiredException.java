package com.best.cvapp.auth.emailverification.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;


public class VerificationTokenExpiredException extends AppException {
    public VerificationTokenExpiredException() {
        super(HttpStatus.BAD_REQUEST, "Verification token has expired. Please request a new one.");
    }
}