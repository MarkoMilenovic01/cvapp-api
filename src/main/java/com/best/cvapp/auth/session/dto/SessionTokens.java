package com.best.cvapp.auth.session.dto;

/**
 * Internal session result passed from the authentication services to the HTTP
 * response adapter. The refresh token must only be transported in an
 * {@code HttpOnly} cookie and must never be serialized as an API response body.
 *
 * @param accessToken JWT returned in the public authentication response
 * @param refreshToken raw refresh token written to the secure cookie boundary
 * @param role authenticated user's role returned in the public response
 */
public record SessionTokens(
        String accessToken,
        String refreshToken,
        String role
) {
}
