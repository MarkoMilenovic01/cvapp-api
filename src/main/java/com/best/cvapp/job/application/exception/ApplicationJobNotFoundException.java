package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ApplicationJobNotFoundException extends AppException {
    public ApplicationJobNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Job not found");
    }
}
