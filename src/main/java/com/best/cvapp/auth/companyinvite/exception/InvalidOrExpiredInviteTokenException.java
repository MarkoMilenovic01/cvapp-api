package com.best.cvapp.auth.companyinvite.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidOrExpiredInviteTokenException extends AppException {

    public InvalidOrExpiredInviteTokenException() {
        super(HttpStatus.BAD_REQUEST, "Invalid or expired invite token");
    }
}