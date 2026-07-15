package com.best.cvapp.auth.session.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class UserAccountDisabledException extends AppException {

    public UserAccountDisabledException() {
        super(HttpStatus.UNAUTHORIZED, "User account is disabled");
    }
}