package com.best.cvapp.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        boolean validLength  = password.length() >= 8 && password.length() <= 128;
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit     = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial   = password.chars().anyMatch(c ->
                "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);

        if (validLength && hasUppercase && hasDigit && hasSpecial) return true;

        context.disableDefaultConstraintViolation();

        if (!validLength)
            context.buildConstraintViolationWithTemplate(
                    "Password must be between 8 and 128 characters").addConstraintViolation();
        else if (!hasUppercase)
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter").addConstraintViolation();
        else if (!hasDigit)
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one digit").addConstraintViolation();
        else
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character").addConstraintViolation();

        return false;
    }
}