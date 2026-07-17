package com.best.cvapp.company.directory.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class DirectoryCompanyNotFoundException extends AppException {

    public DirectoryCompanyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Company not found");
    }
}
