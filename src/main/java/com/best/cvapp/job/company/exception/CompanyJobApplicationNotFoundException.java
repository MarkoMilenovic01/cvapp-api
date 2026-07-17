package com.best.cvapp.job.company.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyJobApplicationNotFoundException extends AppException {
    public CompanyJobApplicationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Application not found");
    }
}
