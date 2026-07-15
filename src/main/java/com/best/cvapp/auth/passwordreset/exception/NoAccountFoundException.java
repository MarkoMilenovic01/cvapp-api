package com.best.cvapp.auth.passwordreset.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class NoAccountFoundException extends AppException {

    public NoAccountFoundException() {
        super(HttpStatus.NOT_FOUND, "No account found with this email");
    }
}