package com.best.cvapp.cv.profile.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CVNotFoundException extends AppException {

    public CVNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CV not found — create your CV first");
    }
}