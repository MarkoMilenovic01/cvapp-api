package com.best.cvapp.admin.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminPageSizeExceededException extends AppException {

    public AdminPageSizeExceededException(int maximumPageSize) {
        super(HttpStatus.BAD_REQUEST, "Page size must not exceed " + maximumPageSize);
    }
}
