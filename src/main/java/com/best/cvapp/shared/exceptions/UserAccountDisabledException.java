package com.best.cvapp.shared.exceptions;

import org.springframework.http.HttpStatus;

public class UserAccountDisabledException extends AppException {

    public UserAccountDisabledException() {
        super(HttpStatus.UNAUTHORIZED, "User account is disabled");
    }
}