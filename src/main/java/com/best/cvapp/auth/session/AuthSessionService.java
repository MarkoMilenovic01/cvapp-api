package com.best.cvapp.auth.session;

import com.best.cvapp.auth.jwt.JwtService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.dto.RefreshTokenRequest;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse createSession(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getRole().name()
        );
    }

//    @Transactional
//    public AuthResponse refresh(RefreshTokenRequest request) {
//        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
//
//        User user = refreshToken.getUser();
//        String newAccessToken = jwtService.generateToken(user);
//
//        return new AuthResponse(
//                newAccessToken,
//                refreshToken.getToken(),
//                user.getRole().name()
//        );
//    }


    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldRefreshToken =
                refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = oldRefreshToken.getUser();

        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is disabled");
        }

        String newAccessToken = jwtService.generateToken(user);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                user.getRole().name()
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        refreshTokenService.deleteByUser(refreshToken.getUser());
    }
}