package com.best.cvapp.auth.token.invitecompany;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteRequest {
    @Email
    @NotBlank
    private String email;
}