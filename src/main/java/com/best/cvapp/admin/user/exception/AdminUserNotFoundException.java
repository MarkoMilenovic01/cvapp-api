package com.best.cvapp.admin.user.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminUserNotFoundException extends AppException {

    public AdminUserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }
}
