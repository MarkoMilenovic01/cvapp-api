package com.best.cvapp.auth.session.dto;


public record AuthResponse(
        String accessToken,
        String role
) {
}
