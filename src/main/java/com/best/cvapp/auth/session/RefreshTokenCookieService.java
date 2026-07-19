package com.best.cvapp.auth.session;

import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.dto.SessionTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Converts an internal session result into the public authentication response.
 * The access token and role are returned in the response body, while the raw
 * refresh token is written only to an {@code HttpOnly} cookie scoped to the
 * authentication endpoints.
 */
@Component
public class RefreshTokenCookieService {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final Duration refreshTokenLifetime;
    private final boolean secure;
    private final String sameSite;

    public RefreshTokenCookieService(
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration,
            @Value("${app.auth.refresh-cookie.secure}") boolean secure,
            @Value("${app.auth.refresh-cookie.same-site:Lax}") String sameSite
    ) {
        this.refreshTokenLifetime = Duration.ofMillis(refreshExpiration);
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /**
     * Returns an authentication response and sets the refresh-token cookie.
     *
     * @param tokens internal tokens created for the authenticated session
     * @return the public response containing the access token and role
     */
    public ResponseEntity<AuthResponse> authenticated(SessionTokens tokens) {
        ResponseCookie cookie = cookie(tokens.refreshToken(), refreshTokenLifetime);
        AuthResponse body = new AuthResponse(tokens.accessToken(), tokens.role());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    /**
     * Returns an empty logout response that expires the refresh-token cookie.
     * Server-side token invalidation must be completed before calling this method.
     *
     * @return a response that clears the refresh-token cookie
     */
    public ResponseEntity<Void> loggedOut() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
