package com.best.cvapp.cv.skill.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class DuplicateSkillException extends AppException {

    public DuplicateSkillException() {
        super(HttpStatus.CONFLICT, "This skill already exists on the CV");
    }
}
