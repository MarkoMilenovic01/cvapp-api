package com.best.cvapp.auth.companyinvite.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InviteAlreadySentException extends AppException {

    public InviteAlreadySentException() {
        super(HttpStatus.CONFLICT, "Invite already sent to this email");
    }
}