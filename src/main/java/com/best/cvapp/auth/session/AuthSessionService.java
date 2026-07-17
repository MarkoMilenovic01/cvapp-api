package com.best.cvapp.auth.session;

import com.best.cvapp.auth.jwt.JwtService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.dto.RefreshTokenRequest;
import com.best.cvapp.auth.session.exception.UserAccountDisabledException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles authenticated sessions.
 *
 * Flow:
 * 1. Create an access token and refresh token after login.
 * 2. Validate and rotate the refresh token when refreshing a session.
 * 3. Delete the refresh token when logging out.
 */
@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse createSession(User user) {
        if (!user.isEnabled()) {
            throw new UserAccountDisabledException();
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, user.getRole().name());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        User user = oldRefreshToken.getUser();

        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
            throw new UserAccountDisabledException();
        }

        refreshTokenService.deleteById(oldRefreshToken.getId());

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken, user.getRole().name());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        refreshTokenService.deleteById(refreshToken.getId());
    }
}
