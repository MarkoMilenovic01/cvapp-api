package com.best.cvapp.cv.education.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class EducationNotFoundException extends AppException {

    public EducationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Education not found");
    }
}