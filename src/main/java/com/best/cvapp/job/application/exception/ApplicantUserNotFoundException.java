package com.best.cvapp.job.application.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ApplicantUserNotFoundException extends AppException {
    public ApplicantUserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }
}
