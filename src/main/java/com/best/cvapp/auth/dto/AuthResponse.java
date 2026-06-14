package com.best.cvapp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response returned after successful register or login.
 * Contains the JWT token and the user's role.
 * Frontend uses the token for all subsequent requests.
 */

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
}