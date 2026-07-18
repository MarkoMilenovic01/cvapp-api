package com.best.cvapp.auth.session;

import com.best.cvapp.auth.session.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final AuthSessionService authSessionService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(RefreshTokenCookieService.COOKIE_NAME) String refreshToken
    ) {
        return refreshTokenCookieService.authenticated(authSessionService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(RefreshTokenCookieService.COOKIE_NAME) String refreshToken
    ) {
        authSessionService.logout(refreshToken);
        return refreshTokenCookieService.loggedOut();
    }
}
