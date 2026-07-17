package com.best.cvapp.admin.user.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class LastEnabledAdministratorException extends AppException {

    public LastEnabledAdministratorException() {
        super(HttpStatus.CONFLICT, "The final enabled administrator cannot be disabled, demoted, or deleted");
    }
}
