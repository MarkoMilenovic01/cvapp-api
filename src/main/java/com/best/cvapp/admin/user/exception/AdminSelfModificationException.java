package com.best.cvapp.admin.user.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminSelfModificationException extends AppException {

    public AdminSelfModificationException(String action) {
        super(HttpStatus.CONFLICT, "Administrators cannot " + action + " their own account");
    }
}
