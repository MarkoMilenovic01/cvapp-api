package com.best.cvapp.shared.exceptions;

import org.springframework.http.HttpStatus;

public class PasswordsDoNotMatchException extends AppException {

    public PasswordsDoNotMatchException() {
        super(HttpStatus.BAD_REQUEST, "Passwords do not match");
    }
}