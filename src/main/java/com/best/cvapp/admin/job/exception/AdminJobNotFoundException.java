package com.best.cvapp.admin.job.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminJobNotFoundException extends AppException {

    public AdminJobNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Job not found");
    }
}
