package com.best.cvapp.job.company.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyJobNotFoundException extends AppException {
    public CompanyJobNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Job not found");
    }
}
