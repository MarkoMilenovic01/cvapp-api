package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class JobDeadlineExpiredException extends AppException {
    public JobDeadlineExpiredException() {
        super(HttpStatus.BAD_REQUEST, "Job application deadline has passed");
    }
}
