package com.best.cvapp.company.favorite.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class FavoriteCVNotFoundException extends AppException {

    public FavoriteCVNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CV not found");
    }
}
