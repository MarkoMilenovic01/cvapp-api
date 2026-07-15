package com.best.cvapp.auth.credentials.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends AppException {

    public EmailAlreadyInUseException() {
        super(HttpStatus.CONFLICT, "Email already in use");
    }
}