package com.best.cvapp.auth.oauth;

import com.best.cvapp.auth.oauth.dto.GoogleLoginRequest;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.RefreshTokenCookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthAuthController {

    private final GoogleAuthService googleAuthService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return refreshTokenCookieService.authenticated(googleAuthService.login(request));
    }
}
