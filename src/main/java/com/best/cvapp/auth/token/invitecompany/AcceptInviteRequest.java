package com.best.cvapp.auth.token.invitecompany;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptInviteRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}