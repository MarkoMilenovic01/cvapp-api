package com.best.cvapp.auth.credentials.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class GoogleAccountLoginRequiredException extends AppException {

    public GoogleAccountLoginRequiredException() {
        super(HttpStatus.CONFLICT, "This email is registered with Google. Please login with Google.");
    }
}