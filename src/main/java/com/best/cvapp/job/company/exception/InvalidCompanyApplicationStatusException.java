package com.best.cvapp.job.company.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCompanyApplicationStatusException extends AppException {
    public InvalidCompanyApplicationStatusException() {
        super(HttpStatus.BAD_REQUEST, "Application status cannot be set by a company");
    }
}
