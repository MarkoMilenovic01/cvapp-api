package com.best.cvapp.auth.passwordreset.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidResetTokenException extends AppException {

    public InvalidResetTokenException() {
        super(HttpStatus.NOT_FOUND, "Invalid reset token");
    }
}