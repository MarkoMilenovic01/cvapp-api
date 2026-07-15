package com.best.cvapp.auth.emailverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank(message = "Verification token is required")
        @Size(max = 36, message = "Verification token is invalid")
        String token
) {
}