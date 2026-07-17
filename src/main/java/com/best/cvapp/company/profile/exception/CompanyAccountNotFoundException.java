package com.best.cvapp.company.profile.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyAccountNotFoundException extends AppException {

    public CompanyAccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Company not found");
    }
}
