package com.best.cvapp.auth.session.dto;

/**
 * Internal session result. The refresh token must only be transported in an
 * HttpOnly cookie and must never be serialized as an API response body.
 */
public record SessionTokens(
        String accessToken,
        String refreshToken,
        String role
) {
}
