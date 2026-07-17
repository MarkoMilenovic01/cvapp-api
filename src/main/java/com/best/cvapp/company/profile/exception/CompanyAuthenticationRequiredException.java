package com.best.cvapp.company.profile.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CompanyAuthenticationRequiredException extends AppException {

    public CompanyAuthenticationRequiredException() {
        super(HttpStatus.UNAUTHORIZED, "Unauthenticated");
    }
}
