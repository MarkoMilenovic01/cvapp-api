package com.best.cvapp.auth.session;

import com.best.cvapp.auth.jwt.JwtService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.dto.RefreshTokenRequest;
import com.best.cvapp.shared.exceptions.UserAccountDisabledException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);


        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getRole().name()
        );
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldRefreshToken =
                refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = oldRefreshToken.getUser();

        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
            throw new UserAccountDisabledException();
        }

        refreshTokenService.deleteById(oldRefreshToken.getId());

        String newAccessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                user.getRole().name()
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(request.refreshToken());

        refreshTokenService.deleteById(refreshToken.getId());
    }
}