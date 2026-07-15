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
 * Creates, refreshes, and ends user sessions.
 *
 * A session consists of a short-lived access token and a refresh token
 * stored in the database (hashed). Refresh tokens are rotated on every
 * use: the old one is deleted and a new one is issued. Access tokens
 * can't be canceled early - once issued, they stay valid until they
 * expire naturally.
 *
 * Flow:
 * 1. Create session - reject disabled accounts, issue an access token
 *                       and a refresh token.
 * 2. Refresh         - validate the refresh token, reject disabled
 *                       accounts (revoking their tokens), rotate the
 *                       refresh token, and issue a new access token.
 * 3. Logout          - validate the refresh token and delete it.
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