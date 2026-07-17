package com.best.cvapp.company.cv.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyCVNotFoundException extends AppException {

    public CompanyCVNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CV not found");
    }
}
