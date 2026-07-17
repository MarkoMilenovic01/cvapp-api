package com.best.cvapp.company.cv.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyCVPageSizeExceededException extends AppException {

    public CompanyCVPageSizeExceededException(int maximumPageSize) {
        super(HttpStatus.BAD_REQUEST, "Page size must not exceed " + maximumPageSize);
    }
}
