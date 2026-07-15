package com.best.cvapp.auth.passwordreset.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ResetTokenAlreadyUsedException extends AppException {

    public ResetTokenAlreadyUsedException() {
        super(HttpStatus.GONE, "Reset token already used");
    }
}