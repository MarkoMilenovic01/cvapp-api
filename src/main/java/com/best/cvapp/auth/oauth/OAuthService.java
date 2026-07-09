package com.best.cvapp.auth.oauth;

import com.best.cvapp.auth.session.dto.AuthResponse; // adjust import to wherever your shared AuthResponse lives
import com.best.cvapp.auth.jwt.JwtService;
import com.best.cvapp.auth.oauth.dto.OAuthExchangeRequest;
import com.best.cvapp.auth.session.RefreshTokenService;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthCodeService oAuthCodeService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse exchangeCode(OAuthExchangeRequest request) {
        Long userId = oAuthCodeService.consumeCode(request.code());
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired authorization code"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User no longer exists"
                ));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getRole().name()
        );
    }
}