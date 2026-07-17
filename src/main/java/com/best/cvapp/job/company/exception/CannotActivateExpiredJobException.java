package com.best.cvapp.job.company.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CannotActivateExpiredJobException extends AppException {
    public CannotActivateExpiredJobException() {
        super(HttpStatus.BAD_REQUEST, "A job with a past deadline cannot be activated");
    }
}
