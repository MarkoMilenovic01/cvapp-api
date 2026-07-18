package com.best.cvapp.auth.credentials;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.credentials.dto.RegisterResponse;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.RefreshTokenCookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CredentialsAuthController {

    private final CredentialsAuthService credentialsAuthService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(credentialsAuthService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return refreshTokenCookieService.authenticated(credentialsAuthService.login(request));
    }
}
