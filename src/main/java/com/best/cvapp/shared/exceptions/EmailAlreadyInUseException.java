package com.best.cvapp.shared.exceptions;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends AppException {

    public EmailAlreadyInUseException() {
        super(HttpStatus.CONFLICT, "Email already in use");
    }
}