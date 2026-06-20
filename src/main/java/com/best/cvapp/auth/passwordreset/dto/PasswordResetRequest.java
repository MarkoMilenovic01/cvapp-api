package com.best.cvapp.auth.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(

        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        @Size(min = 6, max = 128, message = "Confirm password must be between 6 and 128 characters")
        String confirmPassword
) {
}