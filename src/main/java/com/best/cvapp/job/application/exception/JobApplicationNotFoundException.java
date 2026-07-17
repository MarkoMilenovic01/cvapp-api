package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class JobApplicationNotFoundException extends AppException {
    public JobApplicationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Application not found");
    }
}
