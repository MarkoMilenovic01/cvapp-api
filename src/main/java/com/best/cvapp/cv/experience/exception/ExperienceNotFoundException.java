package com.best.cvapp.cv.experience.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ExperienceNotFoundException extends AppException {

    public ExperienceNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Experience not found");
    }
}