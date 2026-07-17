package com.best.cvapp.cv.profile.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CVAccessDeniedException extends AppException {

    public CVAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "CV Access denied");
    }
}