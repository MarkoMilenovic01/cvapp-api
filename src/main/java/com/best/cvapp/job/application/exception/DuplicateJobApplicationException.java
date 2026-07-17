package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class DuplicateJobApplicationException extends AppException {
    public DuplicateJobApplicationException() {
        super(HttpStatus.CONFLICT, "You already applied to this job");
    }
}
