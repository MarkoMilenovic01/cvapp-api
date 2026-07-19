package com.best.cvapp.auth.session.dto;

/**
 * Public authentication response. The refresh token is deliberately excluded
 * because it is transported only in an {@code HttpOnly} cookie.
 *
 * @param accessToken JWT used to authorize API requests
 * @param role authenticated user's role
 */
public record AuthResponse(
        String accessToken,
        String role
) {
}
