package com.best.cvapp.admin.user.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidAdminRoleChangeException extends AppException {

    public InvalidAdminRoleChangeException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
