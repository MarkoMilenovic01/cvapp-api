package com.best.cvapp.company.profile.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyUserNotFoundException extends AppException {

    public CompanyUserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }
}
