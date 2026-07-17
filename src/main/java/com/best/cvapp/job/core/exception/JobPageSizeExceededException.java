package com.best.cvapp.job.core.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class JobPageSizeExceededException extends AppException {
    public JobPageSizeExceededException(int maximumPageSize) {
        super(HttpStatus.BAD_REQUEST, "Page size must not exceed " + maximumPageSize);
    }
}
