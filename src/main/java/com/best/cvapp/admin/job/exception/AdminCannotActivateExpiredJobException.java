package com.best.cvapp.admin.job.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminCannotActivateExpiredJobException extends AppException {

    public AdminCannotActivateExpiredJobException() {
        super(HttpStatus.BAD_REQUEST, "A job with a past deadline cannot be activated");
    }
}
