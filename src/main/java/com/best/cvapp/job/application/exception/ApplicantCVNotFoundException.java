package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ApplicantCVNotFoundException extends AppException {
    public ApplicantCVNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CV not found — create your CV first");
    }
}
