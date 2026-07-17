package com.best.cvapp.cv.project.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProjectNotFoundException extends AppException {

    public ProjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Project not found");
    }
}