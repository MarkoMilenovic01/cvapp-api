package com.best.cvapp.auth.credentials.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        @Size(min = 6, max = 128, message = "Confirm password must be between 6 and 128 characters")
        String confirmPassword
) {
}