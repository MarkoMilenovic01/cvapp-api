package com.best.cvapp.auth.companyinvite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(

        @NotBlank(message = "Invite token is required")
        @Size(max = 36, message = "Invite token is invalid")
        String token,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        @Size(min = 8, max = 128, message = "Confirm password must be between 8 and 128 characters")
        String confirmPassword
) {
}