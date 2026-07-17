package com.best.cvapp.job.listing.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class JobListingCompanyNotFoundException extends AppException {
    public JobListingCompanyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Company not found");
    }
}
