package com.best.cvapp.admin.company.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AdminCompanyNotFoundException extends AppException {

    public AdminCompanyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Company not found");
    }
}
