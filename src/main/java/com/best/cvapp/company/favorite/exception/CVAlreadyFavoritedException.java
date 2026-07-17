package com.best.cvapp.company.favorite.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CVAlreadyFavoritedException extends AppException {

    public CVAlreadyFavoritedException() {
        super(HttpStatus.CONFLICT, "CV already in favorites");
    }
}
