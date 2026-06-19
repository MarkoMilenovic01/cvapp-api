package com.best.cvapp.auth;

import com.best.cvapp.auth.token.invitecompany.AcceptInviteRequest;
import com.best.cvapp.auth.dto.AuthResponse;
import com.best.cvapp.auth.dto.LoginRequest;
import com.best.cvapp.auth.dto.RegisterRequest;
import com.best.cvapp.auth.token.passwordreset.ForgotPasswordRequest;
import com.best.cvapp.auth.token.passwordreset.PasswordResetRequest;
import com.best.cvapp.auth.token.refresh.RefreshTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<AuthResponse> acceptInvite(@RequestBody @Valid AcceptInviteRequest request) {
        return ResponseEntity.ok(authService.acceptInvite(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}