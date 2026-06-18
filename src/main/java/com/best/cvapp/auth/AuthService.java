package com.best.cvapp.auth;

import com.best.cvapp.admin.CompanyInviteService;
import com.best.cvapp.auth.dto.*;
import com.best.cvapp.auth.token.RefreshToken;
import com.best.cvapp.auth.token.RefreshTokenService;
import com.best.cvapp.user.AuthProvider;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CompanyInviteService companyInviteService;

    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (existingUser.getProvider() == AuthProvider.GOOGLE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This email is registered with Google. Please login with Google.");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        });

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(newAccessToken, refreshToken.getToken(), user.getRole().name());
    }

    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        refreshTokenService.deleteByUser(refreshToken.getUser());
    }

    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        return companyInviteService.acceptInvite(request.getToken(), request.getPassword());
    }
}