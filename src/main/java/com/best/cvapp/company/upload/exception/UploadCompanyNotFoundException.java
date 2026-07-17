package com.best.cvapp.company.upload.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class UploadCompanyNotFoundException extends AppException {

    public UploadCompanyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Company not found");
    }
}
