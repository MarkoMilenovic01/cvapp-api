package com.best.cvapp.cv.skill.exception;

import com.best.cvapp.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class SkillNotFoundException extends AppException {

    public SkillNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Skill not found");
    }
}