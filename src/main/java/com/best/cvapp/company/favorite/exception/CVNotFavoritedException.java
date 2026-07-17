package com.best.cvapp.company.favorite.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class CVNotFavoritedException extends AppException {

    public CVNotFavoritedException() {
        super(HttpStatus.NOT_FOUND, "CV not in favorites");
    }
}
