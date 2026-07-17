package com.best.cvapp.job.listing.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ListedJobNotFoundException extends AppException {
    public ListedJobNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Job not found");
    }
}
