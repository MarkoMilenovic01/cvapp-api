package com.best.cvapp.auth.passwordreset.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ResetTokenExpiredException extends AppException {

    public ResetTokenExpiredException() {
        super(HttpStatus.GONE, "Reset token has expired");
    }
}